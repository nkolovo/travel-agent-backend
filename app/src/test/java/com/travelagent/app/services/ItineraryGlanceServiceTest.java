package com.travelagent.app.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.travelagent.app.dto.DateDto;
import com.travelagent.app.dto.DateItemDto;
import com.travelagent.app.dto.ItineraryDto;
import com.travelagent.app.models.Traveler;
import com.travelagent.app.models.User;
import com.travelagent.app.services.ItineraryGlanceService.Day;
import com.travelagent.app.services.ItineraryGlanceService.Entry;
import com.travelagent.app.services.ItineraryGlanceService.Kind;
import com.travelagent.app.services.ItineraryGlanceService.Stop;

import org.junit.jupiter.api.Test;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Parsing tests use descriptions in the same shape planners write them in the app.
 * The render test writes target/glance-preview.pdf so the layout can be eyeballed.
 */
class ItineraryGlanceServiceTest {

    private final ItineraryGlanceService service = new ItineraryGlanceService();

    private static final String FLIGHT = "Airline: Aegean<br>Flight Number: A3985<br>Departing Skopje: 04:05<br>"
            + "Arriving Athens: 06:30<br>Booking reference: <b>9794N7</b><br><br>Online available on this link 24 hours "
            + "prior: https://en.aegeanair.com/#check-in";
    private static final String ARRIVAL_TRANSFER = "Arriving on: A3985 at 06:30<br>Driver: TBA<br>"
            + "Contact: Nikos - +30 690 000 0001<br>Your driver will be monitoring your arrival flight.";
    private static final String HOTEL = "Room / Suite Type: 2 x Standard Double Rooms<br>Reference Number: 58490001<br>"
            + "Check in: September 26, 2026<br>Check out: September 28, 2026<br>Breakfast and taxes included.";
    private static final String TOUR = "Meeting time at your hotel: 08:00<br>Guide: TBA<br>"
            + "Contact: Dimitris - +30 690 000 0002<br>Venture into a 5-hour experience...";
    private static final String GENERAL_INFO = "<div>Monday, Wednesday &amp; Saturday: 09:00-15:00</div>"
            + "<div>Pharmacies open 08:00 until late</div>";
    private static final String FERRY = "Company: Sea Jets<br>Boat: Champion Jet 2<br>Departing Naxos at: 10:35<br>"
            + "Arriving Santorini at: 12:00<br>Reference number: 1112ABCDE";

    @Test
    void flightKeepsDepartureTimeCarrierAndReference() {
        Entry e = service.toEntry(item("Skopje to Athens - Domestic Flight for Helen, Marcus and Anna", null, FLIGHT));
        assertEquals(Kind.FLIGHT, e.kind());
        assertEquals("04:05", e.time());
        assertEquals("Skopje to Athens - Domestic Flight", e.title());
        assertEquals("For Helen, Marcus and Anna  ·  Aegean A3985  ·  Ref 9794N7", e.detail());
    }

    @Test
    void transferWithoutTheWordTransferIsRecognisedByItsDriver() {
        Entry e = service.toEntry(item("Athens Airport to Hotel - for Tina Smith", null, ARRIVAL_TRANSFER));
        assertEquals(Kind.TRANSFER, e.kind());
        assertEquals("06:30", e.time());
        assertEquals("Athens Airport to Hotel", e.title());
        assertEquals("For Tina Smith  ·  Nikos · +30 690 000 0001", e.detail());
    }

    @Test
    void hotelShowsCheckoutAndReferenceButNoTime() {
        Entry e = service.toEntry(item("Athens Ivy Suites", "Hotel", HOTEL));
        assertEquals(Kind.STAY, e.kind());
        assertNull(e.time());
        assertEquals("Until Mon 28 Sep  ·  Ref 58490001", e.detail());
    }

    @Test
    void openingHoursInInfoItemsAreNotMistakenForTimes() {
        assertNull(ItineraryGlanceService.findTime(ItineraryGlanceService.plainText(GENERAL_INFO)));
        assertEquals(Kind.INFO, ItineraryGlanceService.kindOf("Welcome to Athens - Info", null, ""));
    }

    @Test
    void routeAddsUpNightsFromHotelStays() {
        DateDto d1 = date(1L, "2026-09-26", "Arrival in Athens");
        DateDto d2 = date(2L, "2026-09-28", "Athens to Naxos");
        DateItemDto athens = item("Athens Ivy Suites", "Hotel", HOTEL);
        athens.setDate(d1);
        athens.setLocation("Athens");
        DateItemDto naxos = item("Villa Flora Naxos", "Hotel",
                "Reference Number: 1<br>Check in: September 28, 2026<br>Check out: October 02, 2026");
        naxos.setDate(d2);
        naxos.setLocation("Naxos");

        List<Stop> route = service.buildRoute(List.of(d1, d2), List.of(athens, naxos));
        assertEquals(List.of(new Stop("Athens", 2L), new Stop("Naxos", 4L)), route);
    }

    @Test
    void infoItemsMoveToTheReadingListInsteadOfTheTimeline() {
        DateDto d1 = date(1L, "2026-09-26", "Arrival in Athens");
        DateItemDto info = item("Welcome to Athens - Info", null, GENERAL_INFO);
        info.setDate(d1);
        DateItemDto flight = item("Skopje to Athens - Domestic Flight", null, FLIGHT);
        flight.setDate(d1);

        Day day = service.buildDays(List.of(d1), List.of(info, flight)).get(0);
        assertEquals(1, day.entries().size());
        assertEquals(List.of("Welcome to Athens"), day.reading());
    }

    @Test
    void rendersPreviewPdf() throws Exception {
        List<DateDto> dates = new ArrayList<>();
        List<DateItemDto> items = new ArrayList<>();
        String[][] plan = {
                { "2026-09-26", "Arrival in Athens", "Athens",
                        "Walk up to Areopagus Hill at sunset. Ten minutes from your hotel, and the Acropolis glows." },
                { "2026-09-27", "Athens", "Athens", null },
                { "2026-09-28", "Athens to Naxos", "Naxos",
                        "Ask Elizabeth for the bakery behind the Kastro. Their orange cake is the one from the tour." },
                { "2026-09-29", "Naxos", "Naxos", null },
                { "2026-09-30", "Naxos", "Naxos", null },
                { "2026-10-01", "Naxos", "Naxos", null },
                { "2026-10-02", "Naxos to Santorini", "Santorini",
                        "Skip Oia at sunset. The terrace at your hotel has the same view with none of the crowds." },
                { "2026-10-03", "Santorini", "Santorini", null },
                { "2026-10-04", "Santorini", "Santorini", null },
                { "2026-10-05", "Santorini to Athens Departure", "Santorini", null },
        };
        for (int i = 0; i < plan.length; i++) {
            DateDto d = date((long) i + 1, plan[i][0], plan[i][1]);
            d.setLocation(plan[i][2]);
            d.setPlannerNote(plan[i][3]);
            dates.add(d);
        }
        add(items, dates.get(0), 1, "Traveling to Greece - General Information", GENERAL_INFO);
        add(items, dates.get(0), 2, "Veles Hotel to Skopje Airport - Private Transfer for Helen, Marcus and Anna",
                "Pick up time: 02:30<br>Driver: TBA<br>Number: Mika at Plus Transfers - +389 00 000 000");
        add(items, dates.get(0), 3, "Skopje to Athens - Domestic Flight for Helen, Marcus and Anna", FLIGHT);
        add(items, dates.get(0), 4, "Athens Airport to Hotel - for Helen, Marcus and Anna", ARRIVAL_TRANSFER);
        add(items, dates.get(0), 5, "Athens Airport to Hotel - for Tina Smith",
                ARRIVAL_TRANSFER.replace("A3985 at 06:30", "UA422 at 10:35"));
        add(items, dates.get(0), 6, "Athens Ivy Suites", HOTEL).setLocation("Athens");
        add(items, dates.get(0), 7, "Welcome to Athens - Info", "Athens, a City of 3,000 Years and More");
        add(items, dates.get(1), 1, "Acropolis and Flavors of Athens - Private Tour", TOUR);
        add(items, dates.get(2), 1, "Athens Hotel to Airport - Private Transfer",
                "Pick up time: 09:00<br>Driver: TBA<br>Number: Nikos - +30 690 000 0001");
        add(items, dates.get(2), 2, "Athens to Naxos - Domestic flight",
                "Airline: Aegean<br>Flight Number: OA012<br>Departing Athens: 11:30<br>Arriving Naxos: 12:15<br>"
                        + "Booking reference: 8VNNHV");
        add(items, dates.get(2), 3, "Naxos Airport / Port to Hotel - Private Transfer",
                "Arriving on: OA012 at 12:15<br>Driver: TBA<br>Contact: Elizabeth - +30 690 000 0003");
        add(items, dates.get(2), 4, "Villa Flora Naxos",
                "Room / Suite Type: 2 x Junior Suite<br>Reference Number: 58490002<br>"
                        + "Check in: September 28, 2026<br>Check out: October 02, 2026").setLocation("Naxos");
        add(items, dates.get(2), 5, "Naxos Town, Food People and Heritage Walking tour - Private",
                "Meeting Time: 17:30<br>Meeting Location: At your hotel<br>Guide: TBA<br>"
                        + "Contact: Elizabeth - +30 690 000 0003");
        add(items, dates.get(3), 1, "Free Time on Naxos", "The island of Naxos has a history of over 5000 years");
        add(items, dates.get(4), 1, "Villages of Naxos Food Tour - Private",
                "Meeting Time: 10:00<br>Location: At your hotel<br>Contact: Elizabeth - +30 690 000 0003");
        add(items, dates.get(5), 1, "Free Time on Naxos", "Naxos Beaches");
        add(items, dates.get(6), 1, "Naxos Hotel to Airport / Port - Private Transfer",
                "Meeting time at your hotel: 09:45<br>Driver: TBA<br>Contact: Elizabeth - +30 690 000 0003");
        add(items, dates.get(6), 2, "Naxos to Santorini - Ferry Tickets", FERRY);
        add(items, dates.get(6), 3, "Santorini Port to Hotel - Private Transfer",
                "Arriving on: Champion Jet 2 at 12:00<br>Driver: TBA<br>Contact: George - +30 690 000 0004");
        add(items, dates.get(6), 4, "Agali Houses Santorini",
                "Room/Suite type: 2 x Junior Suite Caldera View<br>Reference number: 58490003<br>"
                        + "Check in: October 02, 2026<br>Check out: October 05, 2026").setLocation("Santorini");
        add(items, dates.get(6), 5, "Welcome to Santorini", "Santorini, also known as Thera");
        add(items, dates.get(7), 1, "Discover Santorini - Private Tour",
                "Meeting Time: 10:00<br>Meeting Location: Your hotel<br>Contact: George - +30 690 000 0004");
        add(items, dates.get(8), 1, "Santorini Exclusive Day Cruise - Small Group",
                "Meeting time: 09:30<br>Meeting Location: At Agios Gerasimos Church / Bus Stop<br>"
                        + "Contact: Alexandros +30 22860 00000");
        add(items, dates.get(9), 1, "Santorini Hotel to Airport - Private Transfer",
                "Meeting time at your hotel: 06:30<br>Driver: TBA<br>Contact: George +30 690 000 0004");
        add(items, dates.get(9), 2, "Santorini to Athens - Domestic Flight",
                "Airline: Aegean<br>Flight Number: A3353<br>Departing Santorini: 08:25<br>Arriving Athens: 09:15<br>"
                        + "Booking reference: 8VSEBH");

        ItineraryDto itinerary = new ItineraryDto();
        itinerary.setName("The Cyclades, slowly");
        itinerary.setReservationNumber("PG000000");
        itinerary.setNumTravelers(4);
        itinerary.setArrivalDate(LocalDate.parse("2026-09-26"));
        itinerary.setDepartureDate(LocalDate.parse("2026-10-05"));

        Context context = new Context();
        context.setVariable("itinerary", itinerary);
        context.setVariable("days", service.buildDays(dates, items));
        context.setVariable("route", service.buildRoute(dates, items));
        context.setVariable("plannerName", "Eleni Kostas");
        context.setVariable("plannerFirstName", "Eleni");
        context.setVariable("plannerInitials", "EK");
        context.setVariable("plannerEmail", "eleni@personallytravel.com");
        context.setVariable("plannerPhone", "+30 690 000 0000");

        String html = templateEngine().process("itinerary-glance-pdf", context);
        assertTrue(writePdf(html, true, "glance-preview.pdf") > 1000);
    }

    @Test
    void fullItineraryHidesZeroPriceAndFormatsRealOnes() throws Exception {
        DateDto d1 = date(1L, "2026-09-26", "Arrival in Athens");
        List<DateItemDto> items = new ArrayList<>();
        add(items, d1, 1, "Skopje to Athens - Domestic Flight", FLIGHT).setPdfUrl(
                "http://localhost:8080/api/itineraries/share/token/attachments/1");
        add(items, d1, 2, "Athens Ivy Suites", HOTEL);

        ItineraryDto itinerary = new ItineraryDto();
        itinerary.setName("Helen Smith Itinerary");
        itinerary.setReservationNumber("PG000000");
        itinerary.setLeadName("Smith / Helen");
        itinerary.setNumTravelers(2);
        itinerary.setArrivalDate(LocalDate.parse("2026-09-26"));
        itinerary.setDepartureDate(LocalDate.parse("2026-10-05"));
        itinerary.setDates(List.of(d1));

        Traveler lead = new Traveler();
        lead.setFirstName("Helen");
        lead.setLastName("Smith");
        lead.setLead(true);
        User planner = new User();
        planner.setFullName("Eleni Kostas");
        planner.setEmail("eleni@personallytravel.com");
        planner.setPhoneNumber("+30 690 000 0000");

        Context context = new Context();
        context.setVariable("itinerary", itinerary);
        context.setVariable("travelers", List.of(lead));
        context.setVariable("dateItems", items);
        context.setVariable("user", planner);
        context.setVariable("edgeFadeUrl", "data:image/png;base64," + java.util.Base64.getEncoder().encodeToString(
                getClass().getClassLoader().getResourceAsStream("static/img/edge-fade.png").readAllBytes()));

        String withoutPrice = templateEngine().process("itinerary-pdf", context);
        assertFalse(withoutPrice.contains("Trip Price"));

        itinerary.setTripPrice(7200);
        String withPrice = templateEngine().process("itinerary-pdf", context);
        assertTrue(withPrice.contains("€7,200"));
        assertTrue(writePdf(withPrice, false, "full-preview.pdf") > 1000);
    }

    private static SpringTemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setCharacterEncoding("UTF-8");
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    private static int writePdf(String html, boolean brandFonts, String fileName) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        // Same clean-up the controller does before rendering
        builder.withHtmlContent(html.replace("&nbsp;", "&#160;").replace("<br>", "<br/>")
                .replace("<!doctype html>", "<!DOCTYPE html>").trim(), null);
        if (brandFonts) {
            ItineraryGlanceService.registerBrandFonts(builder);
        }
        builder.toStream(out);
        builder.useFastMode();
        builder.run();

        Path target = Path.of("target", fileName);
        Files.createDirectories(target.getParent());
        Files.write(target, out.toByteArray());
        return out.size();
    }

    private static DateDto date(Long id, String iso, String name) {
        DateDto d = new DateDto();
        d.setId(id);
        d.setDate(iso);
        d.setName(name);
        return d;
    }

    private static DateItemDto item(String name, String category, String description) {
        DateItemDto di = new DateItemDto();
        di.setName(name);
        di.setCategory(category);
        di.setDescription(description);
        di.setPriority((short) 1);
        return di;
    }

    private static DateItemDto add(List<DateItemDto> items, DateDto date, int priority, String name, String description) {
        DateItemDto di = item(name, null, description);
        di.setDate(date);
        di.setPriority((short) priority);
        items.add(di);
        return di;
    }
}

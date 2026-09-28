package com.travelagent.app.services;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.travelagent.app.dto.DateDto;
import com.travelagent.app.dto.DateItemDto;

import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Builds the short "at a glance" view of an itinerary: one compact card per day with a
 * timeline of times, titles and the one line of detail a traveller needs on the go
 * (reference numbers, who to call). Everything is derived from the same date items the
 * full PDF uses, so planners don't enter anything twice.
 */
@Service
public class ItineraryGlanceService {

    public enum Kind {
        FLIGHT("Flight"), FERRY("Ferry"), TRANSFER("Transfer"), STAY("Stay"),
        FREE("Free time"), INFO("Info"), EXPERIENCE("Experience");

        private final String label;

        Kind(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }

        public String cssClass() {
            return name().toLowerCase();
        }
    }

    public record Entry(String time, String title, Kind kind, String detail) {
    }

    public record Day(int number, LocalDate date, String title, String location, List<Entry> entries,
            List<String> reading, String plannerNote) {

        /** Condensed plan for the overview table, e.g. "08:00 Acropolis and Flavors of Athens". */
        public List<String> highlights() {
            return entries.stream()
                    .filter(e -> e.kind() != Kind.INFO)
                    .map(e -> e.time() != null ? e.time() + "  " + e.title() : e.title())
                    .toList();
        }
    }

    public record Stop(String place, Long nights) {
    }

    // Brand fonts are optional: drop the TTFs into src/main/resources/fonts/ and they're picked up.
    // Without them the PDF falls back to the built-in serif/sans fonts.
    private static final String[][] BRAND_FONTS = {
            { "Fraunces-Regular.ttf", "Fraunces", "400", "normal" },
            { "Fraunces-SemiBold.ttf", "Fraunces", "600", "normal" },
            { "Fraunces-Italic.ttf", "Fraunces", "400", "italic" },
            { "DMSans-Regular.ttf", "DM Sans", "400", "normal" },
            { "DMSans-SemiBold.ttf", "DM Sans", "600", "normal" },
            { "DMSans-Bold.ttf", "DM Sans", "700", "normal" },
            { "Caveat-SemiBold.ttf", "Caveat", "600", "normal" },
    };

    private static final Pattern TIME = Pattern.compile("(?<![\\d:])([01]?\\d|2[0-3]):([0-5]\\d)(?![\\d:])(?!\\s*[-–]\\s*\\d)");
    // A time only counts if its line says what it's for, so opening-hours tables in info items are ignored
    private static final Pattern TIME_CONTEXT = Pattern.compile(
            "(?i)(time|meet|depart|arriv|pick|start|board|leav|return|\\bat\\b)");
    private static final Pattern REF = Pattern.compile(
            "(?im)^\\s*(?:booking\\s+)?ref(?:erence)?(?:\\s+(?:number|no\\.?|#))?\\s*:\\s*([A-Za-z0-9-]{4,})");
    private static final Pattern CONTACT = Pattern.compile("(?im)^\\s*(?:contact|number|phone)\\s*:\\s*(.+)$");
    private static final Pattern MEETING_PLACE = Pattern.compile("(?im)^\\s*(?:meeting\\s+)?location\\s*:\\s*(.+)$");
    private static final Pattern CARRIER = Pattern.compile("(?im)^\\s*(?:airline|company)\\s*:\\s*(.+)$");
    private static final Pattern FLIGHT_NO = Pattern.compile("(?im)^\\s*flight\\s+(?:number|no\\.?)\\s*:\\s*(\\S+)");
    private static final Pattern BOAT = Pattern.compile("(?im)^\\s*boat\\s*:\\s*(.+)$");
    private static final Pattern DRIVER = Pattern.compile("(?im)^\\s*driver\\s*:");
    private static final Pattern CHECK_IN =Pattern.compile("(?im)^\\s*check[ -]?in\\s*:\\s*(.+)$");
    private static final Pattern CHECK_OUT = Pattern.compile("(?im)^\\s*check[ -]?out\\s*:\\s*(.+)$");
    private static final Pattern FOR_WHOM = Pattern.compile(
            "\\s*-?\\s*\\bfor\\s+([A-Z][\\p{L}'’.-]*(?:(?:,\\s*|\\s+and\\s+|\\s+&\\s+|\\s+)[A-Z][\\p{L}'’.-]*)*)\\s*$");
    private static final Pattern NOISE_SUFFIX = Pattern.compile(
            "(?i)\\s*-\\s*(private(\\s+(tour|transfer))?|small group|info)\\s*$");

    private static final DateTimeFormatter LONG_DATE = new DateTimeFormatterBuilder()
            .parseCaseInsensitive().appendPattern("MMMM d, yyyy").toFormatter(Locale.ENGLISH);
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("EEE d MMM", Locale.ENGLISH);

    public List<Day> buildDays(List<DateDto> dates, List<DateItemDto> dateItems) {
        List<DateDto> sorted = new ArrayList<>(dates);
        sorted.sort(Comparator.comparing(DateDto::getDate));

        List<Day> days = new ArrayList<>();
        for (int i = 0; i < sorted.size(); i++) {
            DateDto date = sorted.get(i);
            List<Entry> entries = new ArrayList<>();
            List<String> reading = new ArrayList<>();
            dateItems.stream()
                    .filter(di -> di.getDate() != null && date.getId().equals(di.getDate().getId()))
                    .sorted(Comparator.comparing(DateItemDto::getPriority, Comparator.nullsLast(Comparator.naturalOrder())))
                    .forEach(di -> {
                        Entry entry = toEntry(di);
                        if (entry.kind() == Kind.INFO) {
                            reading.add(entry.title());
                        } else {
                            entries.add(entry);
                        }
                    });
            days.add(new Day(i + 1, LocalDate.parse(date.getDate()), date.getName(), date.getLocation(),
                    entries, reading, blankToNull(date.getPlannerNote())));
        }
        return days;
    }

    /** The trip's route from its stays, e.g. Athens (2 nights) › Naxos (4) › Santorini (3). */
    public List<Stop> buildRoute(List<DateDto> dates, List<DateItemDto> dateItems) {
        Map<Long, DateDto> datesById = new java.util.HashMap<>();
        dates.forEach(d -> datesById.put(d.getId(), d));

        List<Stop> stops = new ArrayList<>();
        dateItems.stream()
                .filter(di -> di.getDate() != null && datesById.containsKey(di.getDate().getId()))
                .sorted(Comparator.comparing((DateItemDto di) -> datesById.get(di.getDate().getId()).getDate())
                        .thenComparing(DateItemDto::getPriority, Comparator.nullsLast(Comparator.naturalOrder())))
                .filter(di -> kindOf(di.getName(), di.getCategory(), plainText(di.getDescription())) == Kind.STAY)
                .forEach(di -> {
                    String text = plainText(di.getDescription());
                    String place = firstNonBlank(di.getLocation(), datesById.get(di.getDate().getId()).getLocation(),
                            di.getName());
                    LocalDate in = parseDate(group(CHECK_IN, text));
                    LocalDate out = parseDate(group(CHECK_OUT, text));
                    Long nights = in != null && out != null ? ChronoUnit.DAYS.between(in, out) : null;
                    Stop last = stops.isEmpty() ? null : stops.get(stops.size() - 1);
                    if (last != null && last.place().equalsIgnoreCase(place)) {
                        Long merged = last.nights() != null && nights != null ? last.nights() + nights : last.nights();
                        stops.set(stops.size() - 1, new Stop(last.place(), merged));
                    } else {
                        stops.add(new Stop(place, nights));
                    }
                });
        return stops;
    }

    Entry toEntry(DateItemDto item) {
        String text = plainText(item.getDescription());
        String rawName = item.getName() == null ? "" : HtmlUtils.htmlUnescape(item.getName().replaceAll("<[^>]+>", "")).trim();
        Kind kind = kindOf(rawName, item.getCategory(), text);

        String forWhom = null;
        String title = rawName;
        Matcher who = FOR_WHOM.matcher(title);
        if (who.find()) {
            forWhom = who.group(1);
            title = title.substring(0, who.start());
        }
        title = NOISE_SUFFIX.matcher(title).replaceAll("").trim();

        List<String> detail = new ArrayList<>();
        if (forWhom != null) {
            detail.add("For " + forWhom);
        }
        switch (kind) {
            case FLIGHT -> {
                add(detail, join(" ", group(CARRIER, text), group(FLIGHT_NO, text)));
                add(detail, prefixed("Ref ", group(REF, text)));
            }
            case FERRY -> {
                add(detail, group(CARRIER, text));
                add(detail, group(BOAT, text));
                add(detail, prefixed("Ref ", group(REF, text)));
            }
            case STAY -> {
                LocalDate out = parseDate(group(CHECK_OUT, text));
                add(detail, out != null ? "Until " + out.format(SHORT_DATE) : null);
                add(detail, prefixed("Ref ", group(REF, text)));
            }
            case FREE -> detail.add("Ideas for the day are in your full itinerary");
            default -> {
                add(detail, group(MEETING_PLACE, text));
                add(detail, contact(text));
                add(detail, prefixed("Ref ", group(REF, text)));
            }
        }
        if (kind == Kind.TRANSFER) {
            // Transfers put the useful part (who is meeting you) in the contact line
            detail.removeIf(d -> d.equals(group(MEETING_PLACE, text)));
        }

        String time = kind == Kind.STAY || kind == Kind.FREE || kind == Kind.INFO ? null : findTime(text);
        return new Entry(time, title.isEmpty() ? rawName : title, kind, detail.isEmpty() ? null : String.join("  ·  ", detail));
    }

    static Kind kindOf(String name, String category, String text) {
        String n = name == null ? "" : name.toLowerCase(Locale.ROOT);
        String c = category == null ? "" : category.toLowerCase(Locale.ROOT);
        if (n.contains("flight") || c.contains("flight")) {
            return Kind.FLIGHT;
        }
        if (n.contains("ferry") || c.contains("ferry")) {
            return Kind.FERRY;
        }
        // "Athens Airport to Hotel" items don't always say transfer, but they always name a driver
        if (n.contains("transfer") || c.contains("transfer") || (text != null && DRIVER.matcher(text).find())) {
            return Kind.TRANSFER;
        }
        if (n.contains("free time") || n.startsWith("free ") || c.contains("free")) {
            return Kind.FREE;
        }
        if (n.matches(".*\\binfo(rmation)?\\b.*") || n.startsWith("welcome to") || c.contains("info")) {
            return Kind.INFO;
        }
        if (c.contains("hotel") || c.contains("accommodation") || c.contains("stay")
                || (text != null && CHECK_IN.matcher(text).find())) {
            return Kind.STAY;
        }
        return Kind.EXPERIENCE;
    }

    static String findTime(String text) {
        for (String line : text.split("\n")) {
            Matcher m = TIME.matcher(line);
            while (m.find()) {
                if (TIME_CONTEXT.matcher(line.substring(0, m.start())).find()) {
                    return String.format("%02d:%s", Integer.parseInt(m.group(1)), m.group(2));
                }
            }
        }
        return null;
    }

    /** Description HTML from the rich-text editor, flattened to lines of plain text. */
    static String plainText(String html) {
        if (html == null) {
            return "";
        }
        String text = html
                .replaceAll("(?i)<br\\s*/?>", "\n")
                .replaceAll("(?i)</(p|div|li|h\\d|tr)>", "\n")
                .replaceAll("<[^>]+>", "");
        text = HtmlUtils.htmlUnescape(text).replace(' ', ' ');
        return text.replaceAll("[ \\t]+", " ").replaceAll("\\n\\s*\\n+", "\n").trim();
    }

    private static String contact(String text) {
        String c = group(CONTACT, text);
        return c == null ? null : c.replaceAll("\\s+-\\s+", " · ");
    }

    private static String group(Pattern p, String text) {
        Matcher m = p.matcher(text);
        return m.find() ? blankToNull(m.group(1).trim()) : null;
    }

    private static LocalDate parseDate(String s) {
        if (s == null) {
            return null;
        }
        try {
            return LocalDate.parse(s.trim(), LONG_DATE);
        } catch (DateTimeParseException e) {
            try {
                return LocalDate.parse(s.trim());
            } catch (DateTimeParseException ignored) {
                return null;
            }
        }
    }

    private static void add(List<String> parts, String s) {
        if (s != null && !s.isBlank()) {
            parts.add(s.length() > 80 ? s.substring(0, 79).trim() + "…" : s);
        }
    }

    private static String prefixed(String prefix, String s) {
        return s == null ? null : prefix + s;
    }

    private static String join(String sep, String... parts) {
        String joined = String.join(sep, java.util.Arrays.stream(parts).filter(p -> p != null && !p.isBlank()).toList());
        return joined.isEmpty() ? null : joined;
    }

    private static String firstNonBlank(String... values) {
        for (String v : values) {
            if (v != null && !v.isBlank()) {
                return v.trim();
            }
        }
        return "";
    }

    private static String blankToNull(String s) {
        return s == null || s.isBlank() ? null : s;
    }

    public static void registerBrandFonts(PdfRendererBuilder builder) {
        for (String[] font : BRAND_FONTS) {
            String path = "fonts/" + font[0];
            if (ItineraryGlanceService.class.getClassLoader().getResource(path) == null) {
                continue;
            }
            PdfRendererBuilder.FontStyle style = "italic".equals(font[3])
                    ? PdfRendererBuilder.FontStyle.ITALIC
                    : PdfRendererBuilder.FontStyle.NORMAL;
            builder.useFont(() -> {
                InputStream in = ItineraryGlanceService.class.getClassLoader().getResourceAsStream(path);
                return in;
            }, font[1], Integer.parseInt(font[2]), style, true);
        }
    }
}

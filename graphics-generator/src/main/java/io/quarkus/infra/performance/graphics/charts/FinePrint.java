package io.quarkus.infra.performance.graphics.charts;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.charts.fonts.Alignment;
import io.quarkus.infra.performance.graphics.charts.fonts.FontStyle;
import io.quarkus.infra.performance.graphics.charts.fonts.VAlignment;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.Config;
import io.quarkus.infra.performance.graphics.model.Repo;
import io.quarkus.infra.performance.graphics.model.Timing;

import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.BOLD;
import static io.quarkus.infra.performance.graphics.charts.fonts.FontStyle.PLAIN;
import static io.quarkus.infra.performance.graphics.charts.fonts.Sizer.calculateWidth;
import static io.quarkus.infra.performance.graphics.model.Category.JVM;
import static io.quarkus.infra.performance.graphics.model.Category.NATIVE;
import static io.quarkus.infra.performance.graphics.model.Category.OLD;
import static io.quarkus.infra.performance.graphics.model.Category.QUARKUS;
import static io.quarkus.infra.performance.graphics.model.Category.SPRING;

public class FinePrint implements ElasticElement {
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String SOURCE_CODE_LABEL = "Source:";
    public static final String SPRING_LABEL = "Spring Boot: ";

    private static final int MAXIMUM_FONT_SIZE = 30;
    private static final int MINIMUM_FONT_SIZE = 8;

    private static final int GUTTER = 60;
    private static final int MINIMUM_PADDING = GUTTER / 3;
    private static final int MAXIMUM_PADDING = GUTTER * 3;

    private final Config metadata;
    private final Timing timing;
    private final Label leftLabel;
    private final Label middleLabel;
    private final Label rightLabel;
    private final LabelGroup labelGroup = new LabelGroup();
    private final List<InlinedSVG> svgs = new ArrayList<>();
    private final List<String> leftColumn = new ArrayList<>();
    private final List<String> middleColumn = new ArrayList<>();
    private final List<String> rightColumn = new ArrayList<>();

    public FinePrint(BenchmarkData bmData, String... extraMiddleEntries) {
        this.metadata = bmData.config();
        this.timing = bmData.timing();

        for (String entry : extraMiddleEntries) {
            if (entry.contains(": ")) {
                middleColumn.add(entry);
            } else {
                middleColumn.add("   " + entry);
            }
        }

        if (bmData.group().containsAny(QUARKUS)) {
            if (metadata.quarkus() != null) {
                leftColumn.add("Quarkus: "
                        + metadata.quarkus().version());
            }
        }
        if (bmData.group().containsAny(SPRING)) {
            // This is a bit brittle, but it seems to be the best way of checking if we are plotting two springs or one; the metadata is not sufficient
            if (bmData.group().containsAny(OLD) && metadata.springboot3() != null && metadata.springboot4() != null) {
                leftColumn.add("Spring Boot 3: "
                        + metadata.springboot3().version());
                leftColumn.add("Spring Boot 4: "
                        + metadata.springboot4().version());
            } else {
                if (metadata.springboot() != null) {
                    leftColumn.add(SPRING_LABEL
                            + metadata.springboot().version());
                    // Assume if the group doesn't include old stuff that we want Spring 4
                } else if (metadata.springboot4() != null) {
                    leftColumn.add(SPRING_LABEL
                            + metadata.springboot4().version());

                } else if (metadata.springboot3() != null) { // Edge case, Spring 3 but no Spring 4
                    leftColumn.add(SPRING_LABEL
                            + metadata.springboot3().version());

                }
            }
        }
        if (metadata.jvm() != null) {
            if (bmData.group().containsAny(JVM)) {
                leftColumn.add("JVM: "
                        + metadata.jvm().version());
            }
            if (bmData.group().containsAny(NATIVE)) {
                if (metadata.jvm().graalVM() != null) {
                    leftColumn.add("GraalVM: "
                            + metadata.jvm().graalVM().version());
                }
            }
            if (metadata.jvm().memory() != null) {
                middleColumn.add("Memory: "
                        + metadata.jvm().memory());
            }
            if (bmData.group().containsAny(JVM)) {
                if (metadata.jvm().args() != null) {
                    middleColumn.add("JVM args: "
                            + metadata.jvm().args());
                }
            }
        }

        if (metadata.resources() != null && metadata.resources().appCpus() > 0) {
            middleColumn.add("CPUS: " + metadata.resources().appCpus());
        }

        if (metadata.repo() != null) {
            rightColumn.add(SOURCE_CODE_LABEL + " "
                    + prettyRepoName());
            // Use a few spaces to leave room for a logo

            if (hasScenario(metadata.repo())) {
                rightColumn.add("Scenario: " + metadata.repo().scenario() + "   ");
            }

            if (hasBranch(metadata.repo())) {
                rightColumn.add("Branch: " + metadata.repo().branch());
            }

            if (hasCommit(metadata.repo())) {
                rightColumn.add("Commit: " + metadata.repo().shortCommit());
            }
        }

        // Add date/time
        Optional.ofNullable(this.timing)
                .map(Timing::stop)
                .map(stopTime -> stopTime.atZone(ZoneOffset.UTC).format(DATE_TIME_FORMATTER))
                .ifPresent(stopTime -> rightColumn.add("Execution date: %s".formatted(stopTime)));

        leftLabel = createLabel(leftColumn);

        middleLabel = createLabel(middleColumn);

        rightLabel = createLabel(rightColumn);
    }

    private Label createLabel(List<String> column) {
        return new Label(column.toArray(String[]::new), labelGroup)
                .setHorizontalAlignment(Alignment.LEFT)
                .setVerticalAlignment(VAlignment.TOP)
                .setStyles(new FontStyle[]{BOLD, PLAIN}, ": ");
    }

    @Override
    public int getMaximumVerticalSize() {
        // Allow one line's worth of padding at the top
        return MAXIMUM_FONT_SIZE + getHighestLineCount() * MAXIMUM_FONT_SIZE;
    }

    @Override
    public int getMaximumHorizontalSize() {
        return calculateWidth(leftColumn, MAXIMUM_FONT_SIZE) + calculateWidth(middleColumn, MAXIMUM_FONT_SIZE) + calculateWidth(rightColumn, MAXIMUM_FONT_SIZE) + MAXIMUM_PADDING;
    }

    @Override
    public int getMinimumVerticalSize() {
        // Allow one line's worth of padding at the top
        return MINIMUM_FONT_SIZE + getHighestLineCount() * MINIMUM_FONT_SIZE;
    }

    private int getHighestLineCount() {
        return Math.max(leftColumn.size(), Math.max(middleColumn.size(), rightColumn.size()));
    }

    private Label getLabelWithMostLines() {
        int maxLines = getHighestLineCount();

        if (leftColumn.size() == maxLines) {
            return leftLabel;
        } else if (middleColumn.size() == maxLines) {
            return middleLabel;
        } else {
            return rightLabel;
        }
        
    }

    @Override
    public int getMinimumHorizontalSize() {
        return calculateWidth(leftColumn, MINIMUM_FONT_SIZE) + calculateWidth(middleColumn, MINIMUM_FONT_SIZE) + calculateWidth(rightColumn, MINIMUM_FONT_SIZE) + MINIMUM_PADDING;
    }

    public void draw(Subcanvas g, Theme theme) {
        int targetHeight = g.getHeight();

        int topPadding = calculateTopPadding(targetHeight);

        Subcanvas padded = new Subcanvas(g, g.getWidth(), targetHeight - topPadding, 0, topPadding);

        g.setPaint(theme.text());

        setTargetHeight(padded.getHeight());

        leftLabel.draw(padded);

        int leftLabelWidth = leftLabel.calculateWidth();
        int middleLabelX = leftLabelWidth + GUTTER;
        Subcanvas ml = new Subcanvas(padded, padded.getWidth() - middleLabelX, padded.getHeight(), middleLabelX, 0);
        middleLabel.draw(ml);

        int rightLabelX = leftLabel.calculateWidth() + middleLabel.calculateWidth() + 2 * GUTTER;
        var rl = new Subcanvas(padded, padded.getWidth() - rightLabelX, padded.getHeight(), rightLabelX, 0);
        rightLabel.draw(rl);


        if (metadata.repo() != null) {
            int sourceWidth = rightLabel.calculateWidth(SOURCE_CODE_LABEL, BOLD);

            int logoSize = rightLabel.getAscent();

            // Assume the link is the top entry in the column for ease of calculations
            int logoX = padded.getXOffset() + rightLabelX + sourceWidth + 4;
            int logoY = padded.getYOffset() + rightLabel.getDescent() / 4;
            svgs.add(new InlinedSVGImage(getPath(theme), logoSize, logoX, logoY));

            int linkX = logoX;
            int linkY = padded.getYOffset() + rightLabel.getAscent();
            // We could just do a bunch of spaces, but for accessibility, keep the text on the link
            String repoName = prettyRepoName();
            svgs.add(new InlinedSVGLink(metadata.repo().url(), repoName, linkX, linkY));
        }
    }

    private String prettyRepoName() {
        return metadata.repo().url().replace("https://github.com/", "     ").replaceAll(".git$", "");
    }

    private int calculateTopPadding(int targetHeight) {
        // Add one and a bit line's worth of padding at the top, so it's proportional
        double topPaddingInLines = 1.5;
        double heightOfOneLine = targetHeight / (getHighestLineCount() + topPaddingInLines);
        int topPadding = (int) (topPaddingInLines * heightOfOneLine);
        return topPadding;
    }

    private static boolean hasCommit(Repo repo) {
        return repo.shortCommit() != null;
    }

    private static boolean hasScenario(Repo repo) {
        return repo.scenario() != null;
    }

    private static boolean hasBranch(Repo repo) {
        return (repo.branch() != null) && ! "main".equals(repo.branch());
    }

    private static String getPath(Theme theme) {
        if (theme == Theme.DARK) {
            return "/github-mark-dark.svg";
        } else {
            return "/github-mark-light.svg";
        }
    }

    public Collection<InlinedSVG> getInlinedSVGs() {
        return svgs;
    }

    public int getActualHorizontalSize(int targetHeight) {
        int topPadding = calculateTopPadding(targetHeight);
        int heightLessPadding = targetHeight - topPadding;

        setTargetHeight(heightLessPadding);
        int leftLabelWidth = leftLabel.calculateWidth();
        int middleLabelWidth = middleLabel.calculateWidth();
        int rightLabelWidth = rightLabel.calculateWidth();

        return leftLabelWidth + middleLabelWidth + rightLabelWidth + 2 * GUTTER;

    }

    private void setTargetHeight(int heightLessPadding) {
        // Set target height on the label with the most lines first, so it sets the font for the group
        getLabelWithMostLines().setTargetHeight(heightLessPadding);
    }
}

package io.quarkus.infra.performance.graphics.charts;

import static io.quarkus.infra.performance.graphics.charts.Sizer.calculateWidth;

import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import io.quarkus.infra.performance.graphics.Theme;
import io.quarkus.infra.performance.graphics.VAlignment;
import io.quarkus.infra.performance.graphics.model.BenchmarkData;
import io.quarkus.infra.performance.graphics.model.Config;
import io.quarkus.infra.performance.graphics.model.Repo;
import io.quarkus.infra.performance.graphics.model.Timing;

public class FinePrint implements ElasticElement {
    static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String SOURCE_CODE_LABEL = "Source:";

    private static final int MAXIMUM_FONT_SIZE = 30;
    private static final int MINIMUM_FONT_SIZE = 8;

    private static final int PADDING = 60;
    private static final int MINIMUM_PADDING = PADDING / 3;
    private static final int MAXIMUM_PADDING = PADDING * 3;

    private static final int TOP_PADDING = 30;

    private final Config metadata;
    private final Timing timing;
    private final Label leftLabel;
    private final Label middleLabel;
    private final Label rightLabel;
    private InlinedSVG svg;
    private final List<String> leftColumn = new ArrayList<>();
    private final List<String> middleColumn = new ArrayList<>();
    private final List<String> rightColumn = new ArrayList<>();

    public FinePrint(BenchmarkData bmData) {
        this.metadata = bmData.config();
        this.timing = bmData.timing();

        if (metadata.quarkus() != null) {
            leftColumn.add("Quarkus: "
                    + metadata.quarkus().version());
        }
        if (metadata.springboot() != null) {
            leftColumn.add("Spring: "
                    + metadata.springboot().version());
            // If there's only one Spring, strip the qualifier
            // Assume the truth is in the data, and what's in the data set is what is being plotted
        } else if (metadata.springboot3() != null && metadata.springboot4() == null) {
            leftColumn.add("Spring: "
                    + metadata.springboot3().version());
        } else if (metadata.springboot4() != null && metadata.springboot3() == null) {
            leftColumn.add("Spring: "
                    + metadata.springboot4().version());
        } else if (metadata.springboot3() != null && metadata.springboot4() != null) {
            leftColumn.add("Spring 3: "
                    + metadata.springboot3().version());
            leftColumn.add("Spring 4: "
                    + metadata.springboot4().version());
        }
        if (metadata.jvm() != null) {

            leftColumn.add("JVM: "
                    + metadata.jvm().version());

            if (metadata.jvm().graalVM() != null) {
                leftColumn.add("GraalVM: "
                        + metadata.jvm().graalVM().version());
            }
            if (metadata.jvm().memory() != null) {
                middleColumn.add("Memory: "
                        + metadata.jvm().memory());
            }
            if (metadata.jvm().args() != null) {
                middleColumn.add("JVM args: "
                        + metadata.jvm().args());
            }
        }

        if (metadata.resources() != null) {
            middleColumn.add("CPUS: " + metadata.resources().appCpus());
        }

        if (metadata.repo() != null) {
            rightColumn.add(SOURCE_CODE_LABEL + " "
                    + metadata.repo().url().replace("https://github.com/", "     ").replaceAll(".git$", ""));
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

        // Make sure font sizes are the same
        // TODO this can go away when we have label groups
        var maxRows = Math.max(leftColumn.size(), Math.max(middleColumn.size(), rightColumn.size()));
        adjustToSameRows(leftColumn, maxRows);
        adjustToSameRows(middleColumn, maxRows);
        adjustToSameRows(rightColumn, maxRows);

        leftLabel = new Label(leftColumn.toArray(String[]::new))
                .setHorizontalAlignment(Alignment.LEFT)
                .setVerticalAlignment(VAlignment.TOP);

        middleLabel = new Label(middleColumn.toArray(String[]::new))
                .setHorizontalAlignment(Alignment.LEFT)
                .setVerticalAlignment(VAlignment.TOP);

        rightLabel = new Label(rightColumn.toArray(String[]::new))
                .setHorizontalAlignment(Alignment.LEFT)
                .setVerticalAlignment(VAlignment.TOP);
    }

    private static void adjustToSameRows(List<String> labels, int maxRows) {
      while (labels.size() < maxRows) {
        labels.add(" ");
      }
    }

    @Override
    public int getMaximumVerticalSize() {
        return TOP_PADDING + Math.max(leftColumn.size(), Math.max(middleColumn.size(), rightColumn.size())) * MAXIMUM_FONT_SIZE;
    }

    @Override
    public int getMaximumHorizontalSize() {
        return calculateWidth(leftColumn, MAXIMUM_FONT_SIZE) + calculateWidth(middleColumn, MAXIMUM_FONT_SIZE) + calculateWidth(rightColumn, MAXIMUM_FONT_SIZE) + MAXIMUM_PADDING;
    }

    @Override
    public int getMinimumVerticalSize() {
        return TOP_PADDING + Math.max(leftColumn.size(), Math.max(middleColumn.size(), rightColumn.size())) * MINIMUM_FONT_SIZE;
    }

    @Override
    public int getMinimumHorizontalSize() {
        return calculateWidth(leftColumn, MINIMUM_FONT_SIZE) + calculateWidth(middleColumn, MINIMUM_FONT_SIZE) + calculateWidth(rightColumn, MINIMUM_FONT_SIZE) + MINIMUM_PADDING;
    }

    public void draw(Subcanvas g, Theme theme) {
        g.setPaint(theme.background());
        g.setPaint(theme.text());

        Subcanvas padded = new Subcanvas(g, g.getWidth(), g.getHeight() - TOP_PADDING, 0, TOP_PADDING);

        leftLabel.setTargetHeight(padded.getHeight());
        leftLabel.draw(padded);

        int leftLabelWidth = leftLabel.calculateWidth();
        int middleLabelX = leftLabelWidth + PADDING;
        middleLabel.setTargetHeight(padded.getHeight());
        Subcanvas ml = new Subcanvas(padded, padded.getWidth() - middleLabelX, padded.getHeight(), middleLabelX, 0);
        middleLabel.draw(ml);

        int rightLabelX = middleLabelX + middleLabel.calculateWidth() + PADDING;
        rightLabel.setTargetHeight(padded.getHeight());
        var rl = new Subcanvas(padded, padded.getWidth() - ml.getWidth() - rightLabelX, padded.getHeight(), rightLabelX, 0);
        rightLabel.draw(rl);

        int sw = rightLabel.calculateWidth(SOURCE_CODE_LABEL);

        if (metadata.repo() != null) {
            int logoSize = rightLabel.getAscent();
            int logoX = padded.getXOffset() + rightLabelX + sw + 2;
            int logoY = padded.getYOffset() + rightLabel.getDescent() / 4;
            this.svg = new InlinedSVG(getPath(theme), logoSize,
                    logoX,
                    logoY);
        }
    }

    private static boolean hasCommit(Repo repo) {
      return repo.shortCommit() != null;
    }

    private static boolean hasScenario(Repo repo) {
      return repo.scenario() != null;
    }

    private static boolean hasBranch(Repo repo) {
      return (repo.branch() != null) && !"main".equals(repo.branch());
    }

    private static String getPath(Theme theme) {
        if (theme == Theme.DARK) {
            return "/github-mark-dark.svg";
        } else {
            return "/github-mark-light.svg";
        }
    }

    public Collection<InlinedSVG> getInlinedSVGs() {
        if (svg == null) {
            return Collections.emptyList();
        }
        return List.of(svg);
    }
}

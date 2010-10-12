package org.openshapa.views.component;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

import javax.swing.JComponent;

import org.openshapa.models.component.MixerView;
import org.openshapa.models.component.RegionModel;
import org.openshapa.models.component.Viewport;

import org.openshapa.util.G2DUtils;

import static org.openshapa.models.component.RegionConstants.RMARKER_WIDTH;
import static org.openshapa.models.component.RegionConstants.RMARKER_HEAD_HEIGHT;


/**
 * This class paints the custom playback region.
 */
public final class RegionPainter extends JComponent {

    /** Auto-generated by Eclipse. */
    private static final long serialVersionUID = 3570489696805853386L;

    /** Polygon region for the start marker. */
    private GeneralPath startMarkerPolygon;

    /** Polygon region for the end marker. */
    private GeneralPath endMarkerPolygon;

    /** Region model. */
    private RegionModel regionModel;

    private MixerView mixer;

    /**
     * @return The region model in use.
     */
    public final RegionModel getRegionModel() {
        return regionModel;
    }

    /**
     * @param newRegionModel The new region model to use.
     */
    public final void setRegionModel(final RegionModel newRegionModel) {
        regionModel = newRegionModel;
        repaint();
    }

    public final void setMixerView(final MixerView mixer) {
        this.mixer = mixer;
        repaint();
    }

    /**
     * @return The polygon used to represent the end marker.
     */
    public final GeneralPath getEndMarkerPolygon() {
        return endMarkerPolygon;
    }

    /**
     * @return The polygon used to represent the start marker.
     */
    public final GeneralPath getStartMarkerPolygon() {
        return startMarkerPolygon;
    }

    @Override public final boolean contains(final Point p) {
        return startMarkerPolygon.contains(p) || endMarkerPolygon.contains(p);
    }

    @Override public final boolean contains(final int x, final int y) {
        return ((startMarkerPolygon != null)
                && startMarkerPolygon.contains(x, y))
            || ((endMarkerPolygon != null) && endMarkerPolygon.contains(x, y));
    }

    /**
     * Get the pixel X coordinate for a given timestamp. Takes into account the
     * width of a region marker.
     *
     * @param viewport
     * @param timeInMilliseconds
     * @return
     */
    private static double getXForTime(final Viewport viewport,
        final long timeInMilliseconds) {
        return viewport.computePixelXOffset(timeInMilliseconds) + RMARKER_WIDTH
            + 1;
    }

    @Override public final void paintComponent(final Graphics g) {

        if ((regionModel == null) || (mixer == null)) {
            return;
        }

        Viewport viewport = mixer.getViewport();

        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON);

        Dimension size = this.getSize();

        // If the left region marker is visible, paint the marker
        final long regionStart = regionModel.getRegionStart();
        final long regionEnd = regionModel.getRegionEnd();

        final float needleWidth = 1;
        final float penWidth = 1;

        final Color markerFillColor = new Color(15, 135, 0, 100);
        final Color markerOutlineColor = new Color(15, 135, 0);
        final Color outsideRegionFillColor = new Color(63, 63, 63, 100);

        g2d.setStroke(new BasicStroke(penWidth));

        // If the left region marker is visible, paint the marker
        if (regionStart >= viewport.getViewStart()) {
            final double xPos = getXForTime(viewport,
                    regionModel.getRegionStart());
            startMarkerPolygon = new GeneralPath();
            startMarkerPolygon.moveTo((float) (xPos - RMARKER_WIDTH
                    - needleWidth), 0);
            startMarkerPolygon.lineTo((float) (xPos - needleWidth),
                RMARKER_HEAD_HEIGHT);
            startMarkerPolygon.lineTo((float) (xPos - needleWidth),
                getSize().height - penWidth);
            startMarkerPolygon.lineTo((float) (xPos - RMARKER_WIDTH
                    - needleWidth), getSize().height - penWidth);
            startMarkerPolygon.closePath();

            g2d.setColor(markerFillColor);
            g2d.fill(startMarkerPolygon);

            g2d.setColor(markerOutlineColor);
            g2d.draw(startMarkerPolygon);
        } else {
            startMarkerPolygon = new GeneralPath();
        }

        // If the right region marker is visible, paint the marker
        if ((viewport.getViewStart() <= regionEnd)
                && (regionEnd <= viewport.getViewEnd())) {
            final double xPos = Math.floor(getXForTime(viewport,
                        regionModel.getRegionEnd())) - 1;

            endMarkerPolygon = new GeneralPath();
            endMarkerPolygon.moveTo((float) (xPos + needleWidth),
                RMARKER_HEAD_HEIGHT);
            endMarkerPolygon.lineTo((float) (xPos + RMARKER_WIDTH
                    + needleWidth), 0);
            endMarkerPolygon.lineTo((float) (xPos + RMARKER_WIDTH
                    + needleWidth), getSize().height - penWidth);
            endMarkerPolygon.lineTo((float) (xPos + needleWidth),
                getSize().height - penWidth);
            endMarkerPolygon.closePath();

            g2d.setColor(markerFillColor);
            g2d.fill(endMarkerPolygon);

            g2d.setColor(markerOutlineColor);
            g2d.draw(endMarkerPolygon);
        } else {
            endMarkerPolygon = new GeneralPath();
        }

        /*
         * Check if the selected region is not the maximum viewing window, if it
         * is not the maximum, dim the unplayed regions.
         */
        if (regionStart > 0) {
            final long endTimePos = Math.min(Math.max(regionStart,
                        viewport.getViewStart()), viewport.getViewEnd());

            final double endXPos = getXForTime(viewport, endTimePos);
            final double startXPos = RMARKER_WIDTH;
            final double x = startXPos;
            final double y = RMARKER_HEAD_HEIGHT;
            final double width = endXPos - startXPos - needleWidth;
            final double height = size.height;

            if (width > 0) {
                g2d.setColor(outsideRegionFillColor);
                g2d.fill(G2DUtils.rect(x, y, width, height));
            }
        }

        if (regionEnd < viewport.getViewEnd()) {
            final long startTimePos = Math.min(Math.max(regionEnd,
                        viewport.getViewStart()), viewport.getViewEnd());

            final double startXPos = getXForTime(viewport, startTimePos);
            final double endXPos = getXForTime(viewport, viewport.getViewEnd());
            final double x = startXPos + needleWidth;
            final double y = RMARKER_HEAD_HEIGHT;
            final double width = endXPos - startXPos - needleWidth;
            final double height = size.height;

            if (width > 0) {
                g2d.setColor(outsideRegionFillColor);
                g2d.fill(G2DUtils.rect(x, y, width, height));
            }
        }
    }
}

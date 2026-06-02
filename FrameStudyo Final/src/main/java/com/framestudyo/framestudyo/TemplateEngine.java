package com.framestudyo.framestudyo;

import java.util.ArrayList;
import java.util.List;


public class TemplateEngine {

    // Canvas dimensions assumed by the templates
    private static final double CW = 860;
    private static final double CH = 600;

    // ── Template catalogue ─────────────────────────────────────────────────

    public enum Template {
        BUSINESS_CARD  ("💼 Business Card",  "Name, title, contact layout"),
        POSTER         ("🖼  Poster",         "Headline, sub-text, accent shapes"),
        SOCIAL_POST    ("📱 Social Post",     "Square composition with header"),
        PRESENTATION   ("📊 Slide",           "Title slide with content block"),
        LOGO_DRAFT     ("✦  Logo Draft",      "Centered symbol + wordmark area"),
        BLANK          ("⬡  Blank Canvas",   "Start from scratch");

        private final String displayName;
        private final String description;

        Template(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String displayName() { return displayName; }
        public String description() { return description; }
    }

    public static List<Template> getTemplates() {
        return List.of(Template.values());
    }

    // ── Builder dispatch ───────────────────────────────────────────────────

    public static List<DesignElement> build(Template t) {
        return switch (t) {
            case BUSINESS_CARD -> buildBusinessCard();
            case POSTER        -> buildPoster();
            case SOCIAL_POST   -> buildSocialPost();
            case PRESENTATION  -> buildPresentation();
            case LOGO_DRAFT    -> buildLogoDraft();
            case BLANK         -> new ArrayList<>();
        };
    }

    // ── Templates ──────────────────────────────────────────────────────────

    private static List<DesignElement> buildBusinessCard() {
        List<DesignElement> els = new ArrayList<>();

        // Background card
        RectangleElement bg = new RectangleElement(80, 150, 700, 300);
        bg.setFillColor("#1a1a2e");
        bg.setStrokeColor("#3ddcf8");
        bg.setStrokeWidth(2);
        els.add(bg);

        // Accent bar left
        RectangleElement bar = new RectangleElement(80, 150, 8, 300);
        bar.setFillColor("#ac7cc3");
        bar.setStrokeColor("transparent");
        bar.setStrokeWidth(0);
        els.add(bar);

        // Name text
        TextElement name = new TextElement(140, 220, "Your Name");
        name.setFillColor("#ffffff");
        name.setFontFamily("Segoe UI");
        name.setFontSize(32);
        els.add(name);

        // Title
        TextElement jobTitle = new TextElement(142, 270, "Job Title  |  Company");
        jobTitle.setFillColor("#ac7cc3");
        name.setFontFamily("Segoe UI");
        name.setFontSize(16);
        els.add(jobTitle);

        // Divider line
        LineElement divider = new LineElement(140, 300, 740, 300);
        divider.setStrokeColor("#3ddcf8");
        divider.setStrokeWidth(1);
        els.add(divider);

        // Contact info
        TextElement email = new TextElement(142, 330, "email@example.com");
        email.setFillColor("#cccccc");
        name.setFontFamily("Segoe UI");
        name.setFontSize(13);
        els.add(email);

        TextElement phone = new TextElement(142, 360, "+92 300 0000000");
        phone.setFillColor("#cccccc");
        name.setFontFamily("Segoe UI");
        name.setFontSize(13);
        els.add(phone);

        // Decorative circle (logo placeholder)
        CircleElement logo = new CircleElement(660, 185, 100, 100);
        logo.setFillColor("#ac7cc3");
        logo.setStrokeColor("#3ddcf8");
        logo.setStrokeWidth(2);
        els.add(logo);

        TextElement logoTxt = new TextElement(690, 228, "LOGO");
        logoTxt.setFillColor("#ffffff");
        name.setFontFamily("Segoe UI");
        name.setFontSize(14);
        els.add(logoTxt);

        return els;
    }

    private static List<DesignElement> buildPoster() {
        List<DesignElement> els = new ArrayList<>();

        // Background
        RectangleElement bg = new RectangleElement(40, 20, 780, 560);
        bg.setFillColor("#0f0e17");
        bg.setStrokeColor("#eb6891");
        bg.setStrokeWidth(3);
        els.add(bg);

        // Top accent strip
        RectangleElement strip = new RectangleElement(40, 20, 780, 8);
        strip.setFillColor("#eb6891");
        strip.setStrokeColor("transparent");
        strip.setStrokeWidth(0);
        els.add(strip);

        // Headline
        TextElement headline = new TextElement(120, 120, "YOUR HEADLINE");
        headline.setFillColor("#ffffff");
        headline.setFontFamily("Segoe UI");
        headline.setFontSize(32);
        els.add(headline);

        // Subheadline
        TextElement sub = new TextElement(155, 185, "Subtitle — Date — Location");
        sub.setFillColor("#eb6891");
        headline.setFontFamily("Segoe UI");
        headline.setFontSize(20);
        els.add(sub);

        // Divider line
        LineElement div = new LineElement(100, 210, 760, 210);
        div.setStrokeColor("#eb6891");
        div.setStrokeWidth(1.5);
        els.add(div);

        // Body text placeholder
        TextElement body = new TextElement(115, 260, "Description or body text goes here.");
        body.setFillColor("#aaaaaa");
        body.setFontFamily("Segoe UI");
        body.setFontSize(16);
        els.add(body);

        // Decorative star accent
        StarElement star1 = new StarElement(680, 310, 80, 80);
        star1.setFillColor("#eb6891");
        star1.setStrokeColor("transparent");
        star1.setStrokeWidth(0);
        star1.setOpacity(0.4);
        els.add(star1);

        StarElement star2 = new StarElement(100, 380, 50, 50);
        star2.setFillColor("#3ddcf8");
        star2.setStrokeColor("transparent");
        star2.setStrokeWidth(0);
        star2.setOpacity(0.3);
        els.add(star2);

        // CTA box
        RectangleElement cta = new RectangleElement(280, 460, 300, 60);
        cta.setFillColor("#eb6891");
        cta.setStrokeColor("transparent");
        cta.setStrokeWidth(0);
        els.add(cta);

        TextElement ctaTxt = new TextElement(345, 482, "REGISTER NOW");
        ctaTxt.setFillColor("#ffffff");
        ctaTxt.setFontFamily("Segoe UI");
        ctaTxt.setFontSize(18);
        els.add(ctaTxt);

        return els;
    }

    private static List<DesignElement> buildSocialPost() {
        List<DesignElement> els = new ArrayList<>();

        // Square background (Instagram-ish)
        RectangleElement bg = new RectangleElement(180, 30, 500, 500);
        bg.setFillColor("#16213e");
        bg.setStrokeColor("#ac7cc3");
        bg.setStrokeWidth(2);
        els.add(bg);

        // Header bar
        RectangleElement header = new RectangleElement(180, 30, 500, 70);
        header.setFillColor("#ac7cc3");
        header.setStrokeColor("transparent");
        header.setStrokeWidth(0);
        els.add(header);

        TextElement handle = new TextElement(195, 55, "@yourhandle");
        handle.setFillColor("#ffffff");
        handle.setFontFamily("Segoe UI");
        handle.setFontSize(18);
        els.add(handle);

        // Main image placeholder
        RectangleElement imgPlaceholder = new RectangleElement(200, 120, 460, 260);
        imgPlaceholder.setFillColor("#0f3460");
        imgPlaceholder.setStrokeColor("#3ddcf8");
        imgPlaceholder.setStrokeWidth(1);
        els.add(imgPlaceholder);

        TextElement imgTxt = new TextElement(340, 255, "Image");
        imgTxt.setFillColor("#3ddcf8");
        imgTxt.setFontFamily("Segoe UI");
        imgTxt.setFontSize(20);
        els.add(imgTxt);

        // Caption
        TextElement caption = new TextElement(200, 420, "Your caption goes here ✨");
        caption.setFillColor("#ffffff");
        caption.setFontFamily("Segoe UI");
        caption.setFontSize(15);
        els.add(caption);

        // Hashtag line
        TextElement tags = new TextElement(200, 455, "#design  #creative  #framestudyo");
        tags.setFillColor("#ac7cc3");
        tags.setFontFamily("Segoe UI");
        tags.setFontSize(12);
        els.add(tags);

        return els;
    }

    private static List<DesignElement> buildPresentation() {
        List<DesignElement> els = new ArrayList<>();

        // Slide background
        RectangleElement bg = new RectangleElement(30, 30, 800, 540);
        bg.setFillColor("#ffffff");
        bg.setStrokeColor("#cccccc");
        bg.setStrokeWidth(1);
        els.add(bg);

        // Left accent band
        RectangleElement band = new RectangleElement(30, 30, 12, 540);
        band.setFillColor("#3e7e9f");
        band.setStrokeColor("transparent");
        band.setStrokeWidth(0);
        els.add(band);

        // Title
        TextElement slideTitle = new TextElement(70, 110, "Presentation Title");
        slideTitle.setFillColor("#1a1a2e");
        slideTitle.setFontFamily("Segoe UI");
        slideTitle.setFontSize(36);
        els.add(slideTitle);

        // Underline
        LineElement underline = new LineElement(70, 155, 500, 155);
        underline.setStrokeColor("#3e7e9f");
        underline.setStrokeWidth(3);
        els.add(underline);

        // Subtitle
        TextElement subtitle = new TextElement(70, 185, "Subtitle or presenter name");
        subtitle.setFillColor("#666666");
        subtitle.setFontFamily("Segoe UI");
        subtitle.setFontSize(18);
        els.add(subtitle);

        // Content area placeholder
        RectangleElement content = new RectangleElement(70, 230, 460, 280);
        content.setFillColor("#f5f5f5");
        content.setStrokeColor("#cccccc");
        content.setStrokeWidth(1);
        els.add(content);

        TextElement contentTxt = new TextElement(85, 280, "• Bullet point one");
        contentTxt.setFillColor("#333333");
        contentTxt.setFontFamily("Segoe UI");
        contentTxt.setFontSize(15);
        els.add(contentTxt);

        TextElement contentTxt2 = new TextElement(85, 320, "• Bullet point two");
        contentTxt2.setFillColor("#333333");
        contentTxt2.setFontFamily("Segoe UI");
        contentTxt2.setFontSize(15);
        els.add(contentTxt2);

        TextElement contentTxt3 = new TextElement(85, 360, "• Bullet point three");
        contentTxt3.setFillColor("#333333");
        contentTxt3.setFontFamily("Segoe UI");
        contentTxt3.setFontSize(15);
        els.add(contentTxt3);

        // Image placeholder (right side)
        RectangleElement imgBox = new RectangleElement(560, 230, 240, 280);
        imgBox.setFillColor("#ddeeff");
        imgBox.setStrokeColor("#3e7e9f");
        imgBox.setStrokeWidth(1);
        els.add(imgBox);

        TextElement imgLbl = new TextElement(625, 372, "Image");
        imgLbl.setFillColor("#3e7e9f");
        imgLbl.setFontFamily("Segoe UI");
        imgLbl.setFontSize(16);
        els.add(imgLbl);

        // Footer
        RectangleElement footer = new RectangleElement(30, 545, 800, 25);
        footer.setFillColor("#3e7e9f");
        footer.setStrokeColor("transparent");
        footer.setStrokeWidth(0);
        els.add(footer);

        TextElement footerTxt = new TextElement(40, 552, "FrameStudyo  |  Page 1");
        footerTxt.setFillColor("#ffffff");
        footerTxt.setFontFamily("Segoe UI");
        footerTxt.setFontSize(11);
        els.add(footerTxt);

        return els;
    }

    private static List<DesignElement> buildLogoDraft() {
        List<DesignElement> els = new ArrayList<>();

        // Outer circle (ring)
        CircleElement outerRing = new CircleElement(305, 130, 250, 250);
        outerRing.setFillColor("transparent");
        outerRing.setStrokeColor("#ac7cc3");
        outerRing.setStrokeWidth(4);
        els.add(outerRing);

        // Inner filled circle
        CircleElement inner = new CircleElement(355, 180, 150, 150);
        inner.setFillColor("#ac7cc3");
        inner.setStrokeColor("transparent");
        inner.setStrokeWidth(0);
        els.add(inner);

        // Center star
        StarElement star = new StarElement(385, 210, 90, 90);
        star.setFillColor("#ffffff");
        star.setStrokeColor("transparent");
        star.setStrokeWidth(0);
        els.add(star);

        // Brand name below
        TextElement brand = new TextElement(290, 420, "BRAND NAME");
        brand.setFillColor("#1a1a2e");
        brand.setFontFamily("Segoe UI");
        brand.setFontSize(40);
        els.add(brand);

        // Tagline
        TextElement tagline = new TextElement(330, 468, "Your tagline here");
        tagline.setFillColor("#ac7cc3");
        tagline.setFontFamily("Segoe UI");
        tagline.setFontSize(15);
        els.add(tagline);

        // Decorative lines either side
        LineElement lineL = new LineElement(100, 445, 280, 445);
        lineL.setStrokeColor("#ac7cc3");
        lineL.setStrokeWidth(1.5);
        els.add(lineL);

        LineElement lineR = new LineElement(580, 445, 760, 445);
        lineR.setStrokeColor("#ac7cc3");
        lineR.setStrokeWidth(1.5);
        els.add(lineR);

        return els;
    }
}
package com.eightfold.candidate_transformer_v2.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.*;

/**
 * Generates the final one-page landscape A4 capstone-style design document.
 * Strictly one page. No overflow. Engineering-poster grid layout.
 *
 * Run:  mvnw exec:java -Dexec.mainClass="...util.CapstoneDoc"
 */
public class CapstoneDoc {

    private static final String OUT =
        "C:/Users/HP/Downloads/candidate-transformer-v2/candidate-transformer-v2/";

    // ── Palette ───────────────────────────────────────────────────────────────
    private static final Color NAVY      = new Color(15,  23,  42);   // slate-950
    private static final Color DARK      = new Color(30,  41,  59);   // slate-800
    private static final Color SLATE     = new Color(51,  65,  85);   // slate-700
    private static final Color MID       = new Color(100, 116, 139);  // slate-500
    private static final Color MUTED     = new Color(148, 163, 184);  // slate-400
    private static final Color RULE      = new Color(226, 232, 240);  // slate-200
    private static final Color BG_LIGHT  = new Color(248, 250, 252);  // slate-50
    private static final Color WHITE     = Color.WHITE;
    private static final Color ACCENT    = new Color(37,  99,  235);  // blue-600
    private static final Color ACCENT_LT = new Color(219, 234, 254);  // blue-100
    private static final Color ACCENT_BG = new Color(239, 246, 255);  // blue-50
    private static final Color GREEN_BG  = new Color(240, 253, 244);  // green-50
    private static final Color GREEN_BD  = new Color(187, 247, 208);  // green-200
    private static final Color BORDER    = new Color(203, 213, 225);  // slate-300

    // ── Font helpers ──────────────────────────────────────────────────────────
    private static Font f(int sz, int style, Color c) { return new Font(Font.HELVETICA, sz, style, c); }
    private static final Font F_TITLE  = f(16, Font.BOLD,   NAVY);
    private static final Font F_SUB    = f( 7, Font.NORMAL, MID);
    private static final Font F_TAG    = f( 6, Font.NORMAL, MUTED);
    private static final Font F_SH     = f( 7, Font.BOLD,   ACCENT);
    private static final Font F_BODY   = f( 6, Font.NORMAL, SLATE);
    private static final Font F_BOLD   = f( 6, Font.BOLD,   DARK);
    private static final Font F_MUTED  = f( 5, Font.NORMAL, MUTED);
    private static final Font F_TH     = f( 6, Font.BOLD,   WHITE);
    private static final Font F_TC     = f( 6, Font.NORMAL, DARK);
    private static final Font F_TCB    = f( 6, Font.BOLD,   SLATE);
    private static final Font F_STEP_N = f( 7, Font.BOLD,   ACCENT);
    private static final Font F_STEP_L = f( 6, Font.BOLD,   DARK);
    private static final Font F_STEP_D = f( 6, Font.NORMAL, SLATE);
    private static final Font F_BADGE  = f( 6, Font.BOLD,   ACCENT);
    private static final Font F_CHK    = f( 6, Font.NORMAL, DARK);
    private static final Font F_FTRW   = f( 5, Font.NORMAL, MUTED);

    // ── Canvas font helpers ───────────────────────────────────────────────────
    private BaseFont HB, HN, HI;
    private void initFonts() throws Exception {
        HB = BaseFont.createFont(BaseFont.HELVETICA_BOLD,    BaseFont.CP1252, false);
        HN = BaseFont.createFont(BaseFont.HELVETICA,         BaseFont.CP1252, false);
        HI = BaseFont.createFont(BaseFont.HELVETICA_OBLIQUE, BaseFont.CP1252, false);
    }

    public static void main(String[] args) throws Exception {
        new CapstoneDoc().run();
    }

    public void run() throws Exception {
        initFonts();
        generateDoc();
        System.out.println("Generated: " + OUT + "Candidate_Transformer_AI_Design_Document.pdf");
    }

    // =========================================================================
    // DOCUMENT — A4 Landscape, one page
    // =========================================================================
    private void generateDoc() throws Exception {
        // A4 landscape: 842 × 595 pt.  Margins: L36 R36 T32 B28
        Rectangle page = PageSize.A4.rotate();
        Document doc = new Document(page, 36, 36, 32, 28);
        PdfWriter w = PdfWriter.getInstance(doc,
            new FileOutputStream(OUT + "Candidate_Transformer_AI_Design_Document.pdf"));
        w.setPageEvent(new FooterEvent());
        doc.open();

        float PW = page.getWidth();   // 842
        float PH = page.getHeight();  // 595

        // ── HEADER STRIP ─────────────────────────────────────────────────────
        addHeader(doc, PW);

        // ── MAIN GRID  3 columns × 2 rows ────────────────────────────────────
        //  Col widths: 190 | 200 | 190   gap = 8
        //  Total used: 190+8+200+8+190 = 596  (fits in 842-72=770 available)
        // We use a PdfPTable with 3 columns for the grid body
        PdfPTable grid = new PdfPTable(3);
        grid.setWidthPercentage(100);
        grid.setWidths(new float[]{1.0f, 1.1f, 1.0f});
        grid.setSpacingBefore(5);
        grid.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        grid.getDefaultCell().setPadding(0);

        // Row 1
        grid.addCell(card(buildOverview(), 4));
        grid.addCell(card(buildArchDiagram(), 4));
        grid.addCell(card(buildWorkflowStrip(), 4));

        // Row 2
        grid.addCell(card(buildTechStack(), 3));
        grid.addCell(card(buildDesignDecisions(), 3));
        grid.addCell(card(buildEdgesAndFuture(), 3));

        doc.add(grid);
        doc.close();
    }

    // ── HEADER ───────────────────────────────────────────────────────────────
    private void addHeader(Document doc, float pw) throws DocumentException {
        PdfPTable hdr = new PdfPTable(2);
        hdr.setWidthPercentage(100);
        hdr.setWidths(new float[]{3f, 1f});
        hdr.setSpacingAfter(0);

        // Left: title block
        PdfPCell left = new PdfPCell();
        left.setBorder(Rectangle.BOTTOM);
        left.setBorderColorBottom(ACCENT);
        left.setBorderWidthBottom(1.5f);
        left.setPaddingBottom(6);
        left.setPaddingLeft(0);
        left.setPaddingRight(12);

        Paragraph title = new Paragraph("Candidate Transformer AI", F_TITLE);
        title.setSpacingAfter(1);
        left.addElement(title);
        Paragraph sub = new Paragraph(
            "AI-Powered Candidate Intelligence Platform  ·  Spring Boot 3  ·  Java 21  ·  Apache PDFBox  ·  Apache Commons CSV  ·  Thymeleaf",
            F_SUB);
        sub.setSpacingAfter(2);
        left.addElement(sub);
        Paragraph desc = new Paragraph(
            "Transforms a recruiter CSV and a candidate Resume PDF into a unified, AI-analysed candidate profile — in a single HTTP request.", F_TAG);
        left.addElement(desc);

        // Right: meta card
        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.BOTTOM);
        right.setBorderColorBottom(RULE);
        right.setBorderWidthBottom(1.5f);
        right.setPaddingBottom(6);
        right.setPaddingLeft(10);
        right.setVerticalAlignment(Element.ALIGN_BOTTOM);

        String[][] meta = {{"Backend","Spring Boot 3 / Java 21"},{"Architecture","Layered MVC"},
            {"Input","CSV + Resume PDF"},{"Output","Profile + JSON"},{"AI","Rule-based Analyzer"}};
        PdfPTable mt = new PdfPTable(2);
        mt.setWidthPercentage(100);
        mt.setWidths(new float[]{1f, 1.5f});
        for (String[] row : meta) {
            PdfPCell k = bare(new Phrase(row[0], F_TCB), false);
            k.setPaddingBottom(1); k.setPaddingTop(1);
            PdfPCell v = bare(new Phrase(row[1], F_TC), false);
            v.setPaddingBottom(1); v.setPaddingTop(1);
            mt.addCell(k); mt.addCell(v);
        }
        right.addElement(mt);

        hdr.addCell(left);
        hdr.addCell(right);
        doc.add(hdr);
    }

    // ── SECTION CONTENT BUILDERS ─────────────────────────────────────────────

    /** Section 1 — Project Overview */
    private Element buildOverview() throws DocumentException {
        PdfPTable t = section("PROJECT OVERVIEW");
        t.addCell(bodyRow("Recruiters spend significant time manually reviewing resumes and cross-referencing spreadsheets before shortlisting candidates."));
        t.addCell(bodyRow("This system accepts a structured Recruiter CSV and an unstructured Resume PDF, then automatically parses, normalises, merges both sources, and runs an AI analysis pipeline — producing a unified candidate record in under two seconds."));
        t.addCell(bodyRow("The output is a recruiter-ready profile page and a downloadable JSON report that a hiring manager can review without any technical context."));
        t.addCell(bodyRow("Business value: eliminates manual resume review, reduces screening time, and produces consistent, bias-free candidate summaries at scale."));
        return t;
    }

    /** Section 2 — Architecture Diagram (drawn inline as PdfPTable) */
    private Element buildArchDiagram() throws DocumentException {
        PdfPTable outer = section("SYSTEM ARCHITECTURE");

        // Build a mini diagram as nested table
        PdfPTable diag = new PdfPTable(1);
        diag.setWidthPercentage(100);

        Object[][] nodes = {
            {"Recruiter",                        "Upload CSV + Resume PDF",       "actor"},
            {"\u2193", "", "arrow"},
            {"Transform Controller",             "POST /transform",               "ctrl"},
            {"\u2193", "", "arrow"},
            {"Transform Service",                "Orchestration layer",           "svc"},
            {"\u2193", "", "arrow"},
            {"CSV Parser  \u00B7  Resume Parser","Commons CSV  \u00B7  PDFBox",   "util"},
            {"\u2193", "", "arrow"},
            {"Normalizer  \u00B7  Merger",       "Canonical data + field merge",  "util"},
            {"\u2193", "", "arrow"},
            {"AI Analyzer",                      "Role \u00B7 Score \u00B7 Gaps \u00B7 Summary", "svc"},
            {"\u2193", "", "arrow"},
            {"Candidate Profile  \u00B7  JSON Export", "Thymeleaf  \u00B7  /download/json", "out"},
        };

        for (Object[] n : nodes) {
            String lbl = (String)n[0], sub = (String)n[1], type = (String)n[2];
            if (type.equals("arrow")) {
                PdfPCell ac = bare(new Phrase(lbl, f(8, Font.NORMAL, MUTED)), true);
                ac.setPaddingTop(0); ac.setPaddingBottom(0);
                diag.addCell(ac);
                continue;
            }
            Color bg, bd; Font lf;
            switch (type) {
                case "actor": bg=NAVY;       bd=NAVY;       lf=f(6,Font.BOLD,WHITE); break;
                case "ctrl":  bg=ACCENT_BG;  bd=ACCENT_LT;  lf=f(6,Font.BOLD,DARK);  break;
                case "svc":   bg=ACCENT_BG;  bd=ACCENT_LT;  lf=f(6,Font.BOLD,DARK);  break;
                case "out":   bg=GREEN_BG;   bd=GREEN_BD;   lf=f(6,Font.BOLD,DARK);  break;
                default:      bg=BG_LIGHT;   bd=BORDER;     lf=f(6,Font.BOLD,DARK);  break;
            }
            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(bg); cell.setBorderColor(bd); cell.setBorderWidth(0.4f);
            cell.setPaddingTop(3); cell.setPaddingBottom(2);
            cell.setPaddingLeft(6); cell.setPaddingRight(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            Paragraph lp = new Paragraph(lbl, lf); lp.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(lp);
            if (!sub.isEmpty()) {
                Paragraph sp = new Paragraph(sub, type.equals("actor")
                    ? f(5,Font.NORMAL,new Color(148,163,184)) : F_MUTED);
                sp.setAlignment(Element.ALIGN_CENTER);
                cell.addElement(sp);
            }
            diag.addCell(cell);
        }

        PdfPCell wrap = bare(null, false);
        wrap.addElement(diag);
        outer.addCell(wrap);
        return outer;
    }

    /** Section 3 — Workflow (numbered vertical strip) */
    private Element buildWorkflowStrip() throws DocumentException {
        PdfPTable t = section("END-TO-END WORKFLOW");
        String[][] steps = {
            {"1","Upload",   "CSV + Resume PDF via web form"},
            {"2","Parse",    "CSV headers mapped; PDF split into sections"},
            {"3","Normalise","Names, emails, phones, skills standardised"},
            {"4","Merge",    "CSV metadata + resume content unified"},
            {"5","Analyse",  "9 role clusters scored; match score computed"},
            {"6","Profile",  "Recruiter-friendly report rendered in browser"},
            {"7","Export",   "Clean JSON served as named download file"},
        };
        for (String[] s : steps) {
            PdfPTable row = new PdfPTable(3);
            row.setWidthPercentage(100);
            row.setWidths(new float[]{0.28f, 0.55f, 1.5f});
            row.setSpacingAfter(1.5f);

            PdfPCell num = bare(new Phrase(s[0], f(7,Font.BOLD,WHITE)), false);
            num.setBackgroundColor(ACCENT); num.setBorderColor(ACCENT); num.setBorderWidth(0.3f);
            num.setHorizontalAlignment(Element.ALIGN_CENTER);
            num.setPaddingTop(2); num.setPaddingBottom(2); num.setPaddingLeft(2); num.setPaddingRight(2);

            PdfPCell lbl = bare(new Phrase(s[1], F_BOLD), false);
            lbl.setPaddingLeft(4); lbl.setPaddingTop(2); lbl.setPaddingBottom(2);

            PdfPCell dsc = bare(new Phrase(s[2], F_BODY), false);
            dsc.setPaddingLeft(4); dsc.setPaddingTop(2); dsc.setPaddingBottom(2);

            row.addCell(num); row.addCell(lbl); row.addCell(dsc);
            PdfPCell wrap = bare(null, false); wrap.addElement(row);
            t.addCell(wrap);
        }
        return t;
    }

    /** Section 4 — Technology Stack */
    private Element buildTechStack() throws DocumentException {
        PdfPTable t = section("TECHNOLOGY STACK");

        // badges row
        String[][] techs = {
            {"Java 21"},{"Spring Boot 3"},{"Apache PDFBox"},
            {"Commons CSV"},{"Jackson"},{"Thymeleaf"},{"Bootstrap 5"},{"Maven"},
        };
        // 2-col table of badges
        PdfPTable bt = new PdfPTable(2);
        bt.setWidthPercentage(100);
        bt.setWidths(new float[]{1f, 1f});
        for (String[] tech : techs) {
            PdfPCell bc = new PdfPCell(new Phrase(tech[0], F_BADGE));
            bc.setBackgroundColor(ACCENT_BG);
            bc.setBorderColor(ACCENT_LT); bc.setBorderWidth(0.4f);
            bc.setPaddingTop(3); bc.setPaddingBottom(3);
            bc.setPaddingLeft(5); bc.setPaddingRight(5);
            bc.setHorizontalAlignment(Element.ALIGN_CENTER);
            bt.addCell(bc);
        }
        PdfPCell bw = bare(null, false); bw.addElement(bt);
        t.addCell(bw);

        // small rule
        t.addCell(ruleCell());

        // Key features (check list — 2 col)
        PdfPCell slKf = bare(null, false); slKf.addElement(secLabel("KEY FEATURES"));
        t.addCell(slKf);
        String[] feats = {
            "\u2713  CSV Upload",          "\u2713  Resume PDF Upload",
            "\u2713  Section-based Parsing","\u2713  Data Normalisation",
            "\u2713  CSV + Resume Merge",   "\u2713  AI Recruiter Insights",
            "\u2713  Candidate Profile UI", "\u2713  Recruiter JSON Export",
        };
        PdfPTable ft = new PdfPTable(2);
        ft.setWidthPercentage(100);
        ft.setWidths(new float[]{1f,1f});
        for (String feat : feats) {
            PdfPCell fc = bare(new Phrase(feat, F_CHK), false);
            fc.setPaddingTop(1.5f); fc.setPaddingBottom(1.5f);
            ft.addCell(fc);
        }
        PdfPCell fw = bare(null, false); fw.addElement(ft);
        t.addCell(fw);
        return t;
    }

    /** Section 5 — Design Decisions */
    private Element buildDesignDecisions() throws DocumentException {
        PdfPTable t = section("KEY DESIGN DECISIONS");
        Object[][] decisions = {
            {"\u25B6  Section-based resume parsing",
             "Splitting the PDF by section headings prevents content from one section — such as certifications or project descriptions — from being misclassified as technical skills."},
            {"\u25B6  Technical skill whitelist",
             "Matching parsed tokens against a curated list of ~120 real technologies eliminates false positives like internship titles, certificate names, and random text."},
            {"\u25B6  Structured + unstructured data merge",
             "Combining the recruiter's CSV (contact metadata) with the resume (skills, education, experience) provides a richer, unified candidate record than either source alone."},
            {"\u25B6  Rule-based recruiter insights",
             "Scoring candidate skills across nine predefined role clusters runs entirely in-process — zero API cost, deterministic results, and works fully offline."},
            {"\u25B6  Recruiter-readable JSON export",
             "The JSON report is assembled from parsed data into human-readable sections so a hiring manager can understand the candidate profile without technical knowledge."},
        };
        for (Object[] d : decisions) {
            Paragraph p = new Paragraph();
            p.add(new Chunk((String)d[0] + "\n", F_BOLD));
            p.add(new Chunk((String)d[1], F_BODY));
            p.setSpacingAfter(4);
            p.setLeading(9);
            PdfPCell c = bare(null, false);
            c.addElement(p);
            t.addCell(c);
        }
        return t;
    }

    /** Section 6 — Edge Cases + Future */
    private Element buildEdgesAndFuture() throws DocumentException {
        PdfPTable t = section("EDGE CASES HANDLED");
        String[] ec = {
            "\u2713  Fresher resume — yearsExperience = 0, labelled \"Fresher\"",
            "\u2713  Resume without Skills section — returns empty skill list",
            "\u2713  Certifications adjacent to Skills — stop condition blocks extraction",
            "\u2713  Missing email or phone — stored empty, no null crash in view",
            "\u2713  Duplicate skills — deduplicated and sorted alphabetically",
            "\u2713  Missing CSV fields — graceful empty-string fallback, no crash",
        };
        for (String e : ec) {
            t.addCell(bodyRow(e));
        }

        t.addCell(ruleCell());
        PdfPCell slFi = bare(null, false); slFi.addElement(secLabel("FUTURE IMPROVEMENTS"));
        t.addCell(slFi);
        String[] fi = {
            "\u2192  Job Description Matching — rank candidates against a specific JD",
            "\u2192  LLM Candidate Summary — GPT-powered narrative instead of rule-based",
            "\u2192  Batch Resume Processing — multiple PDFs in a single session",
        };
        for (String f : fi) {
            Paragraph p = new Paragraph(f, F_BODY);
            p.setSpacingAfter(3); p.setLeading(9);
            PdfPCell c = bare(null, false); c.addElement(p);
            t.addCell(c);
        }
        return t;
    }

    // =========================================================================
    // LAYOUT HELPERS
    // =========================================================================

    /** Creates a card cell with thin border and padding, containing a section. */
    private PdfPCell card(Element content, float topPad) throws DocumentException {
        PdfPCell c = new PdfPCell();
        c.setBorderColor(RULE); c.setBorderWidth(0.5f);
        c.setBackgroundColor(WHITE);
        c.setPaddingTop(topPad); c.setPaddingBottom(5);
        c.setPaddingLeft(8); c.setPaddingRight(8);
        c.addElement(content);
        return c;
    }

    /** Returns a 1-column PdfPTable pre-populated with a section heading row. */
    private PdfPTable section(String heading) throws DocumentException {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(0); t.setSpacingAfter(0);

        PdfPCell hc = new PdfPCell();
        hc.setBorder(Rectangle.BOTTOM);
        hc.setBorderColorBottom(ACCENT); hc.setBorderWidthBottom(0.8f);
        hc.setPaddingBottom(3); hc.setPaddingLeft(0); hc.setPaddingRight(0);
        hc.setPaddingTop(0);

        Paragraph hp = new Paragraph();
        hp.add(new Chunk("\u25FC  ", f(5, Font.NORMAL, ACCENT)));
        hp.add(new Chunk(heading, F_SH));
        hc.addElement(hp);
        t.addCell(hc);
        return t;
    }

    /** Adds a secondary sub-label inside a section */
    private PdfPTable secLabel(String text) throws DocumentException {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(4); t.setSpacingAfter(2);
        PdfPCell c = bare(new Phrase(text, f(6, Font.BOLD, MID)), false);
        c.setPaddingBottom(2);
        t.addCell(c);
        return t;
    }

    /** A simple body text row */
    private PdfPCell bodyRow(String text) {
        Paragraph p = new Paragraph(text, F_BODY);
        p.setSpacingAfter(2.5f); p.setLeading(9);
        PdfPCell c = bare(null, false);
        c.setPaddingTop(2);
        c.addElement(p);
        return c;
    }

    /** A thin horizontal rule row */
    private PdfPCell ruleCell() {
        PdfPCell c = new PdfPCell(new Phrase(""));
        c.setBorder(Rectangle.BOTTOM); c.setBorderColorBottom(RULE);
        c.setBorderWidthBottom(0.4f);
        c.setFixedHeight(6);
        c.setPaddingBottom(0);
        return c;
    }

    /** A borderless cell, optionally center-aligned */
    private PdfPCell bare(Phrase content, boolean center) {
        PdfPCell c = content != null ? new PdfPCell(content) : new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(0);
        if (center) c.setHorizontalAlignment(Element.ALIGN_CENTER);
        return c;
    }

    // ── Page footer ───────────────────────────────────────────────────────────
    class FooterEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter w, Document d) {
            try {
                PdfContentByte cb = w.getDirectContent();
                float pw = d.getPageSize().getWidth(), bot = d.bottomMargin();
                cb.setColorStroke(RULE); cb.setLineWidth(0.4f);
                cb.moveTo(d.leftMargin(), bot + 12);
                cb.lineTo(pw - d.rightMargin(), bot + 12); cb.stroke();

                cb.beginText();
                cb.setColorFill(MUTED);
                cb.setFontAndSize(HN, 5.5f);
                cb.showTextAligned(Element.ALIGN_LEFT,
                    "Candidate Transformer AI  \u00B7  AI-Powered Candidate Intelligence Platform  \u00B7  Spring Boot 3 / Java 21",
                    d.leftMargin(), bot + 4, 0);
                cb.showTextAligned(Element.ALIGN_RIGHT,
                    "Design Document  \u00B7  Hiring Assessment Submission",
                    pw - d.rightMargin(), bot + 4, 0);
                cb.endText();
            } catch (Exception ignored) {}
        }
    }
}

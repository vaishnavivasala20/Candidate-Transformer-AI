package com.eightfold.candidate_transformer_v2.util;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.awt.Color;
import java.io.*;

/**
 * Generates the final one-page Design Document PDF + architecture diagram +
 * workflow diagram.  All content is derived from the actual source code.
 *
 * Run: mvnw exec:java -Dexec.mainClass="...util.FinalDesignDoc"
 */
public class FinalDesignDoc {

    private static final String OUT =
        "C:/Users/HP/Downloads/candidate-transformer-v2/candidate-transformer-v2/";

    // Palette — white bg, near-black body, single blue accent, grays only
    private static final Color INK        = new Color(15,  23,  42);   // slate-900
    private static final Color HEADING    = new Color(30,  41,  59);   // slate-800
    private static final Color SUB        = new Color(71,  85,  105);  // slate-600
    private static final Color MUTED      = new Color(148, 163, 184);  // slate-400
    private static final Color RULE       = new Color(226, 232, 240);  // slate-200
    private static final Color ACCENT     = new Color(37,  99,  235);  // blue-600
    private static final Color ACCENT_BG  = new Color(239, 246, 255);  // blue-50
    private static final Color ROW_ALT    = new Color(248, 250, 252);  // slate-50
    private static final Color WHITE      = Color.WHITE;
    private static final Color BOX_BORDER = new Color(203, 213, 225);  // slate-300
    private static final Color GREEN_BOX  = new Color(240, 253, 244);  // green-50
    private static final Color GREEN_BDR  = new Color(187, 247, 208);  // green-200

    // Fonts
    private static final Font FT   = font(22, Font.BOLD,   HEADING);
    private static final Font FST  = font( 9, Font.NORMAL, SUB);
    private static final Font FH   = font( 8, Font.BOLD,   ACCENT);
    private static final Font FB   = font( 7, Font.NORMAL, INK);
    private static final Font FBB  = font( 7, Font.BOLD,   HEADING);
    private static final Font FG   = font( 7, Font.NORMAL, SUB);
    private static final Font FM   = font( 6, Font.NORMAL, MUTED);
    private static final Font FTH  = font( 7, Font.BOLD,   WHITE);
    private static final Font FTC  = font( 7, Font.NORMAL, INK);
    private static final Font FTL  = font( 7, Font.BOLD,   SUB);
    private static final Font FSTEP= font( 7, Font.BOLD,   ACCENT);
    private static final Font FARW = font(10, Font.NORMAL, MUTED);
    private static final Font FNB  = font( 8, Font.BOLD,   HEADING);
    private static final Font FNS  = font( 7, Font.NORMAL, SUB);

    private static Font font(int sz, int style, Color c) {
        return new Font(Font.HELVETICA, sz, style, c);
    }

    public static void main(String[] args) throws Exception {
        new FinalDesignDoc().run();
    }

    public void run() throws Exception {
        generatePdf();
        generateArchPdf();
        generateWorkflowPdf();
        System.out.println("Done. Files written to: " + OUT);
    }

    // =========================================================================
    // MAIN ONE-PAGE DOCUMENT
    // =========================================================================
    private void generatePdf() throws Exception {
        // A4 with tight margins to fit everything on one page
        Document doc = new Document(PageSize.A4, 36, 36, 30, 30);
        PdfWriter w = PdfWriter.getInstance(doc, new FileOutputStream(OUT + "Candidate_Transformer_AI_Design_Document.pdf"));
        w.setPageEvent(new Footer());
        doc.open();

        // ── HEADER ───────────────────────────────────────────────────────────
        Paragraph title = new Paragraph("Candidate Transformer AI", FT);
        title.setSpacingAfter(2);
        doc.add(title);

        Paragraph subtitle = new Paragraph("AI-Powered Candidate Intelligence Platform  ·  Spring Boot 3  ·  Java 21", FST);
        subtitle.setSpacingAfter(3);
        doc.add(subtitle);

        Paragraph tagline = new Paragraph(
            "Transforms a recruiter CSV and a candidate resume PDF into a unified, AI-analysed candidate profile.", FG);
        tagline.setSpacingAfter(5);
        doc.add(tagline);

        doc.add(hRule(ACCENT, 1.2f, 0, 4));

        // ── TWO-COLUMN BODY ───────────────────────────────────────────────────
        PdfPTable body = new PdfPTable(2);
        body.setWidthPercentage(100);
        body.setWidths(new float[]{1f, 1f});

        PdfPCell L = col(6, 0, 0, 0);   // left  — padding right
        PdfPCell R = col(0, 0, 0, 6);   // right — padding left
        R.setBorderWidthLeft(0.4f);
        R.setBorderColorLeft(RULE);

        buildLeft(L);
        buildRight(R);

        body.addCell(L);
        body.addCell(R);
        doc.add(body);

        doc.close();
        System.out.println("  PDF: Candidate_Transformer_AI_Design_Document.pdf");
    }

    // ── LEFT COLUMN ──────────────────────────────────────────────────────────
    private void buildLeft(PdfPCell c) throws DocumentException {

        // 1. PROJECT OVERVIEW
        c.addElement(sectionHead("1  Project Overview"));
        c.addElement(bodyLine("Recruiters spend hours manually reading resumes and cross-referencing spreadsheets before shortlisting candidates."));
        c.addElement(bodyLine("This system accepts a structured CSV (recruiter metadata) and an unstructured Resume PDF, then automatically parses, normalises, and merges both sources into a single unified candidate record."));
        c.addElement(bodyLine("An in-process AI analyser scores the candidate across nine technical domains, computes a match score (0–100), and produces a recruiter-ready report in under two seconds."));
        c.addElement(bodyLine("The output is a clean, shareable JSON profile that a hiring manager can review without any technical context."));
        c.addElement(gap(5));

        // 2. TECH STACK
        c.addElement(sectionHead("2  Technology Stack"));
        c.addElement(buildTechTable());
        c.addElement(gap(5));

        // 4. WORKFLOW
        c.addElement(sectionHead("4  End-to-End Workflow"));
        String[][] steps = {
            {"1", "Upload",     "Recruiter submits a CSV and Resume PDF via the web form."},
            {"2", "Parse",      "CSV headers are mapped; the PDF is split into named sections (Skills, Experience, Education, Projects, Certifications)."},
            {"3", "Normalise",  "Names, emails, phones, and skill aliases are standardised to canonical forms."},
            {"4", "Merge",      "CSV metadata (headline, location) and resume content are combined under a single Candidate object."},
            {"5", "Analyse",    "Candidate skills are scored across nine role clusters to determine fit, gaps, and a 0–100 match score."},
            {"6", "Profile",    "A recruiter-friendly candidate report is rendered in the browser via Thymeleaf."},
            {"7", "Export",     "A clean, pretty-printed JSON report is downloaded with the candidate's name in the filename."},
        };
        for (String[] s : steps) {
            Paragraph p = new Paragraph();
            p.add(new Chunk(s[0] + ". ", FSTEP));
            p.add(new Chunk(s[1] + " — ", FBB));
            p.add(new Chunk(s[2], FB));
            p.setSpacingAfter(2.5f);
            p.setLeading(10);
            c.addElement(p);
        }
    }

    // ── RIGHT COLUMN ─────────────────────────────────────────────────────────
    private void buildRight(PdfPCell c) throws DocumentException {

        // 3. ARCHITECTURE DIAGRAM
        c.addElement(sectionHead("3  System Architecture"));
        c.addElement(buildArchDiagram());
        c.addElement(gap(5));

        // 5. KEY DESIGN DECISIONS
        c.addElement(sectionHead("5  Key Design Decisions"));
        Object[][] decisions = {
            {"Section-based resume parsing",
             "Splitting the PDF by section headings prevents experience descriptions and certificate names from contaminating the skills list."},
            {"Technical skill whitelist",
             "Matching tokens only against a curated ~120-entry list eliminates false positives such as internship titles and project names."},
            {"Resume + CSV merge strategy",
             "Contact metadata comes from the structured CSV while all unstructured data (skills, education, projects) is always sourced from the resume."},
            {"Rule-based recruiter insights",
             "Scoring candidate skills against predefined role clusters runs entirely in-process — zero API cost, zero latency, works offline."},
            {"Recruiter-friendly JSON export",
             "A separate report object is assembled from parsed data so the downloaded file reads like a human-written profile, not a serialised Java object."},
        };
        for (Object[] d : decisions) {
            Paragraph p = new Paragraph();
            p.add(new Chunk("\u25B6  " + d[0] + " — ", FBB));
            p.add(new Chunk((String) d[1], FG));
            p.setSpacingAfter(3.5f);
            p.setLeading(10);
            c.addElement(p);
        }
        c.addElement(gap(5));

        // 6. EDGE CASES
        c.addElement(sectionHead("6  Edge Cases Handled"));
        String[] ec = {
            "Fresher resume — zero experience reported, labelled \"Fresher\" in output.",
            "Missing email or phone — stored as empty, no null-pointer in the view.",
            "Duplicate skills — deduplicated and sorted alphabetically before display.",
            "Resume without a Skills section — returns an empty skill list, no guessing.",
            "Certifications adjacent to Skills — section stop condition blocks extraction.",
            "Missing CSV fields — graceful empty-string fallback, no crash.",
        };
        for (String e : ec) {
            Paragraph p = new Paragraph("\u2713  " + e, FB);
            p.setSpacingAfter(2.5f);
            p.setLeading(10);
            c.addElement(p);
        }
        c.addElement(gap(5));

        // FUTURE IMPROVEMENTS
        c.addElement(sectionHead("Future Improvements"));
        String[] fi = {
            "JD Matching — score each candidate against a specific job description.",
            "LLM Candidate Summary — replace rule-based summary with a GPT-generated narrative.",
            "Batch Resume Processing — support multiple PDF uploads in a single session.",
        };
        for (String f : fi) {
            Paragraph p = new Paragraph("\u2022  " + f, FG);
            p.setSpacingAfter(2f);
            p.setLeading(10);
            c.addElement(p);
        }
    }

    // ── TECH TABLE ────────────────────────────────────────────────────────────
    private PdfPTable buildTechTable() throws DocumentException {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{1.0f, 1.8f});
        t.setSpacingBefore(2); t.setSpacingAfter(2);
        // header row
        tblHdr(t, "Layer"); tblHdr(t, "Technology");
        // data
        String[][] rows = {
            {"Backend",        "Java 21, Spring Boot 3.5"},
            {"Frontend",       "Thymeleaf, Bootstrap 5"},
            {"PDF Parsing",    "Apache PDFBox 2.0.30"},
            {"CSV Parsing",    "Apache Commons CSV 1.10"},
            {"JSON",           "Jackson Databind"},
            {"Build Tool",     "Maven"},
            {"Server",         "Embedded Apache Tomcat"},
        };
        boolean alt = false;
        for (String[] r : rows) {
            tblCell(t, r[0], alt, true);
            tblCell(t, r[1], alt, false);
            alt = !alt;
        }
        return t;
    }

    private void tblHdr(PdfPTable t, String text) {
        PdfPCell c = new PdfPCell(new Phrase(text, FTH));
        c.setBackgroundColor(HEADING);
        c.setBorderColor(HEADING); c.setBorderWidth(0.4f);
        c.setPaddingTop(3); c.setPaddingBottom(3);
        c.setPaddingLeft(5); c.setPaddingRight(5);
        t.addCell(c);
    }
    private void tblCell(PdfPTable t, String text, boolean alt, boolean bold) {
        PdfPCell c = new PdfPCell(new Phrase(text, bold ? FTL : FTC));
        c.setBackgroundColor(alt ? ROW_ALT : WHITE);
        c.setBorderColor(RULE); c.setBorderWidth(0.4f);
        c.setPaddingTop(2.5f); c.setPaddingBottom(2.5f);
        c.setPaddingLeft(5); c.setPaddingRight(5);
        t.addCell(c);
    }

    // ── INLINE ARCHITECTURE DIAGRAM ──────────────────────────────────────────
    private PdfPTable buildArchDiagram() throws DocumentException {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(2); t.setSpacingAfter(2);

        // Each row: [label, sublabel, style]
        // styles: actor | primary | util | output | arrow | split
        Object[][] rows = {
            {"Recruiter",                    "Upload CSV + Resume PDF",        "actor"},
            {"\u2193",                        "",                              "arrow"},
            {"Transform Controller",         "POST /transform",               "primary"},
            {"\u2193",                        "",                              "arrow"},
            {"Transform Service",            "Orchestration",                 "primary"},
            {"\u2193",                        "",                              "arrow"},
            {"CSV Parser  \u00B7  Resume Parser", "Commons CSV  \u00B7  PDFBox sections", "util"},
            {"\u2193",                        "",                              "arrow"},
            {"Normalizer  \u00B7  Merger",   "Canonical data + field merge",  "util"},
            {"\u2193",                        "",                              "arrow"},
            {"AI Analyzer",                  "Role \u00B7 Match Score \u00B7 Gaps \u00B7 Summary", "primary"},
            {"\u2193",                        "",                              "arrow"},
            {"Candidate Profile  \u00B7  JSON Export", "Thymeleaf view  \u00B7  GET /download/json", "output"},
        };

        for (Object[] row : rows) {
            String label = (String) row[0];
            String sub   = (String) row[1];
            String style = (String) row[2];

            if (style.equals("arrow")) {
                PdfPCell ac = new PdfPCell(new Phrase(label, FARW));
                ac.setBorder(Rectangle.NO_BORDER);
                ac.setHorizontalAlignment(Element.ALIGN_CENTER);
                ac.setPaddingTop(0); ac.setPaddingBottom(0);
                t.addCell(ac);
                continue;
            }

            Color bg, bdr;
            Font lf;
            switch (style) {
                case "actor":  bg = HEADING;    bdr = HEADING;    lf = font(8, Font.BOLD, WHITE);   break;
                case "primary":bg = ACCENT_BG;  bdr = new Color(191,219,254); lf = FNB; break;
                case "output": bg = GREEN_BOX;  bdr = GREEN_BDR;  lf = FNB;              break;
                default:       bg = ROW_ALT;    bdr = BOX_BORDER; lf = FNB;              break;
            }

            PdfPCell cell = new PdfPCell();
            cell.setBackgroundColor(bg);
            cell.setBorderColor(bdr); cell.setBorderWidth(0.5f);
            cell.setPaddingTop(4); cell.setPaddingBottom(3);
            cell.setPaddingLeft(8); cell.setPaddingRight(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);

            Paragraph lp = new Paragraph(label, lf);
            lp.setAlignment(Element.ALIGN_CENTER);
            cell.addElement(lp);

            if (!sub.isEmpty()) {
                Paragraph sp = new Paragraph(sub, style.equals("actor") ? font(6, Font.NORMAL, new Color(191,219,254)) : FM);
                sp.setAlignment(Element.ALIGN_CENTER);
                cell.addElement(sp);
            }
            t.addCell(cell);
        }
        return t;
    }

    // =========================================================================
    // STANDALONE ARCHITECTURE DIAGRAM (A4 landscape, hi-res)
    // =========================================================================
    private void generateArchPdf() throws Exception {
        Rectangle page = PageSize.A4.rotate();
        Document doc = new Document(page, 55, 55, 45, 45);
        PdfWriter w = PdfWriter.getInstance(doc, new FileOutputStream(OUT + "Architecture_Diagram.pdf"));
        doc.open();
        PdfContentByte cb = w.getDirectContent();
        float pw = page.getWidth(), ph = page.getHeight();

        // Title bar
        cb.setColorFill(HEADING); cb.rectangle(0, ph - 52, pw, 52); cb.fill();
        txt(cb, "Candidate Transformer AI — System Architecture", pw/2, ph - 28, 14, Font.BOLD, WHITE, Element.ALIGN_CENTER);
        txt(cb, "Spring Boot 3  ·  Java 21  ·  Section-Based PDF Parsing  ·  Rule-Based AI Analyzer",
            pw/2, ph - 42, 8, Font.NORMAL, new Color(148,163,184), Element.ALIGN_CENTER);

        float cx = pw / 2;
        float y = ph - 100;
        float bw = 200, bh = 42, gap = 22;

        // Boxes
        box(cb, cx - bw/2, y, bw, bh, HEADING, HEADING);
        txt(cb, "Recruiter", cx, y + bh/2 + 5, 10, Font.BOLD, WHITE, Element.ALIGN_CENTER);
        txt(cb, "Upload CSV  +  Resume PDF", cx, y + bh/2 - 8, 8, Font.NORMAL, new Color(148,163,184), Element.ALIGN_CENTER);

        y -= (bh + gap); arrow(cb, cx, y + bh + gap, cx, y + bh);
        box(cb, cx - bw/2, y, bw, bh, new Color(239,246,255), new Color(191,219,254));
        txt(cb, "Transform Controller", cx, y + bh/2 + 5, 9, Font.BOLD, HEADING, Element.ALIGN_CENTER);
        txt(cb, "POST /transform", cx, y + bh/2 - 8, 8, Font.NORMAL, SUB, Element.ALIGN_CENTER);

        y -= (bh + gap); arrow(cb, cx, y + bh + gap, cx, y + bh);
        box(cb, cx - bw/2, y, bw, bh, new Color(239,246,255), new Color(191,219,254));
        txt(cb, "Transform Service", cx, y + bh/2 + 5, 9, Font.BOLD, HEADING, Element.ALIGN_CENTER);
        txt(cb, "Orchestrates the full pipeline", cx, y + bh/2 - 8, 8, Font.NORMAL, SUB, Element.ALIGN_CENTER);

        // Split to two parsers
        float splitY = y - (bh + gap);
        float lpx = cx - 130, rpx = cx + 130 - bw;
        cb.setColorStroke(MUTED); cb.setLineWidth(0.8f);
        cb.moveTo(cx, y); cb.lineTo(cx, splitY + bh + 10); cb.stroke();
        cb.moveTo(lpx + bw/2, splitY + bh + 10); cb.lineTo(rpx + bw/2, splitY + bh + 10); cb.stroke();
        arrowDown(cb, lpx + bw/2, splitY + bh + 10, splitY + bh);
        arrowDown(cb, rpx + bw/2, splitY + bh + 10, splitY + bh);

        box(cb, lpx, splitY, bw, bh, ROW_ALT, BOX_BORDER);
        txt(cb, "CSV Parser", lpx + bw/2, splitY + bh/2 + 5, 9, Font.BOLD, HEADING, Element.ALIGN_CENTER);
        txt(cb, "Apache Commons CSV  ·  header-mapped", lpx + bw/2, splitY + bh/2 - 8, 7, Font.NORMAL, SUB, Element.ALIGN_CENTER);

        box(cb, rpx, splitY, bw, bh, ROW_ALT, BOX_BORDER);
        txt(cb, "Resume Parser", rpx + bw/2, splitY + bh/2 + 5, 9, Font.BOLD, HEADING, Element.ALIGN_CENTER);
        txt(cb, "Apache PDFBox  ·  section-based", rpx + bw/2, splitY + bh/2 - 8, 7, Font.NORMAL, SUB, Element.ALIGN_CENTER);

        // Merge back
        y = splitY - (bh + gap);
        cb.moveTo(lpx + bw/2, splitY); cb.lineTo(lpx + bw/2, y + bh + 10); cb.stroke();
        cb.moveTo(rpx + bw/2, splitY); cb.lineTo(rpx + bw/2, y + bh + 10); cb.stroke();
        cb.moveTo(lpx + bw/2, y + bh + 10); cb.lineTo(rpx + bw/2, y + bh + 10); cb.stroke();
        arrowDown(cb, cx, y + bh + 10, y + bh);

        box(cb, cx - bw/2, y, bw, bh, ROW_ALT, BOX_BORDER);
        txt(cb, "Normalizer  ·  Merger", cx, y + bh/2 + 5, 9, Font.BOLD, HEADING, Element.ALIGN_CENTER);
        txt(cb, "Canonical forms  ·  CSV + Resume unified", cx, y + bh/2 - 8, 7, Font.NORMAL, SUB, Element.ALIGN_CENTER);

        y -= (bh + gap); arrow(cb, cx, y + bh + gap, cx, y + bh);
        box(cb, cx - bw/2, y, bw, bh, new Color(239,246,255), new Color(191,219,254));
        txt(cb, "AI Analyzer", cx, y + bh/2 + 5, 9, Font.BOLD, HEADING, Element.ALIGN_CENTER);
        txt(cb, "Role  ·  Match Score  ·  Gap Analysis  ·  Summary", cx, y + bh/2 - 8, 7, Font.NORMAL, SUB, Element.ALIGN_CENTER);

        // Two outputs
        float outL = cx - 120, outR = cx + 20;
        float outY = y - (bh + gap);
        cb.moveTo(cx, y); cb.lineTo(cx, outY + bh + 10); cb.stroke();
        cb.moveTo(outL + bw/2, outY + bh + 10); cb.lineTo(outR + bw/2, outY + bh + 10); cb.stroke();
        arrowDown(cb, outL + bw/2, outY + bh + 10, outY + bh);
        arrowDown(cb, outR + bw/2, outY + bh + 10, outY + bh);

        box(cb, outL, outY, bw, bh, GREEN_BOX, GREEN_BDR);
        txt(cb, "Candidate Profile", outL + bw/2, outY + bh/2 + 5, 9, Font.BOLD, HEADING, Element.ALIGN_CENTER);
        txt(cb, "Thymeleaf  ·  index.html", outL + bw/2, outY + bh/2 - 8, 7, Font.NORMAL, SUB, Element.ALIGN_CENTER);

        box(cb, outR, outY, bw, bh, GREEN_BOX, GREEN_BDR);
        txt(cb, "JSON Export", outR + bw/2, outY + bh/2 + 5, 9, Font.BOLD, HEADING, Element.ALIGN_CENTER);
        txt(cb, "GET /download/json", outR + bw/2, outY + bh/2 - 8, 7, Font.NORMAL, SUB, Element.ALIGN_CENTER);

        // Legend
        float lx = 55, ly = 42;
        txt(cb, "Legend:", lx, ly + 14, 7, Font.BOLD, SUB, Element.ALIGN_LEFT);
        box(cb, lx + 45, ly, 12, 12, HEADING, HEADING);
        txt(cb, "User / Actor", lx + 62, ly + 10, 7, Font.NORMAL, SUB, Element.ALIGN_LEFT);
        box(cb, lx + 140, ly, 12, 12, new Color(239,246,255), new Color(191,219,254));
        txt(cb, "Spring Layer", lx + 157, ly + 10, 7, Font.NORMAL, SUB, Element.ALIGN_LEFT);
        box(cb, lx + 240, ly, 12, 12, ROW_ALT, BOX_BORDER);
        txt(cb, "Processing", lx + 257, ly + 10, 7, Font.NORMAL, SUB, Element.ALIGN_LEFT);
        box(cb, lx + 335, ly, 12, 12, GREEN_BOX, GREEN_BDR);
        txt(cb, "Output", lx + 352, ly + 10, 7, Font.NORMAL, SUB, Element.ALIGN_LEFT);

        doc.close();
        System.out.println("  PDF: Architecture_Diagram.pdf");
    }

    // =========================================================================
    // STANDALONE WORKFLOW DIAGRAM (A4 portrait)
    // =========================================================================
    private void generateWorkflowPdf() throws Exception {
        Document doc = new Document(PageSize.A4, 55, 55, 45, 45);
        PdfWriter w = PdfWriter.getInstance(doc, new FileOutputStream(OUT + "Workflow_Diagram.pdf"));
        doc.open();
        PdfContentByte cb = w.getDirectContent();
        float pw = PageSize.A4.getWidth(), ph = PageSize.A4.getHeight();

        // Title bar
        cb.setColorFill(HEADING); cb.rectangle(0, ph - 52, pw, 52); cb.fill();
        txt(cb, "Candidate Transformer AI — End-to-End Workflow", pw/2, ph - 28, 13, Font.BOLD, WHITE, Element.ALIGN_CENTER);
        txt(cb, "Seven-step pipeline from file upload to JSON export",
            pw/2, ph - 42, 8, Font.NORMAL, new Color(148,163,184), Element.ALIGN_CENTER);

        String[][] steps = {
            {"1","Upload",
             "Recruiter submits a Recruiter CSV and a Resume PDF through the web upload form.",
             "upload.html  ·  POST /transform"},
            {"2","Parse",
             "CSV is read with header-aware column mapping. The PDF is segmented into named sections — Skills, Experience, Education, Projects, Certifications — before extraction.",
             "CsvParser  ·  ResumeParser  ·  PDFBox"},
            {"3","Normalise",
             "All fields are standardised: names to Title Case, emails to lowercase, 10-digit Indian numbers prefixed +91, and skill aliases resolved to canonical names.",
             "Normalizer"},
            {"4","Merge",
             "Contact metadata from the CSV (headline, location) is combined with unstructured resume data. The resume always takes precedence for skills, education, and experience.",
             "Merger  ·  Field priority rules"},
            {"5","Analyse",
             "Candidate skills are scored against nine role clusters (Java, Frontend, Cyber Security, DevOps, AI/ML…) to determine the best-fit role, compute a 0–100 match score, identify skill gaps, and generate a professional summary.",
             "AIAnalyzer  ·  Rule-based engine"},
            {"6","Profile",
             "The merged and analysed candidate record is stored in the HTTP session and rendered as a recruiter-friendly profile page in the browser.",
             "TransformController  ·  index.html"},
            {"7","Export",
             "On request, a clean JSON report is assembled from the parsed data and served as a downloadable file named after the candidate (e.g. John_Doe_Candidate_Profile.json).",
             "DownloadController  ·  Jackson"},
        };

        float spineX = pw / 2;
        float startY = ph - 105;
        float stepH  = 73;
        float bw = 220, bh = 56;

        // Vertical spine
        cb.setColorStroke(RULE); cb.setLineWidth(1.5f);
        cb.moveTo(spineX, startY + 12);
        cb.lineTo(spineX, startY - (steps.length - 1) * stepH - bh/2);
        cb.stroke();

        for (int i = 0; i < steps.length; i++) {
            float yc = startY - i * stepH;
            String[] s = steps[i];
            boolean left = (i % 2 == 0);

            // Circle on spine
            cb.setColorFill(ACCENT); cb.setColorStroke(ACCENT); cb.setLineWidth(0.5f);
            cb.circle(spineX, yc, 11); cb.fillStroke();
            txt(cb, s[0], spineX, yc - 4, 9, Font.BOLD, WHITE, Element.ALIGN_CENTER);

            // Horizontal connector
            float boxEdge = left ? spineX - 16 : spineX + 16;
            float boxX    = left ? boxEdge - bw : boxEdge;
            cb.setColorStroke(RULE); cb.setLineWidth(0.8f);
            float hEnd = left ? boxEdge - bw + bw : boxEdge;
            cb.moveTo(left ? boxEdge : spineX + 11, yc);
            cb.lineTo(left ? spineX - 11 : boxEdge, yc);
            cb.stroke();

            // Box
            Color bg = (i == 0) ? new Color(239,246,255) : (i == steps.length-1) ? GREEN_BOX : WHITE;
            Color bd = (i == 0) ? new Color(191,219,254) : (i == steps.length-1) ? GREEN_BDR : RULE;
            box(cb, boxX, yc - bh/2, bw, bh, bg, bd);

            int ta = left ? Element.ALIGN_RIGHT : Element.ALIGN_LEFT;
            float tx = left ? boxX + bw - 8 : boxX + 8;

            txt(cb, s[1], tx, yc + 18, 10, Font.BOLD, HEADING, ta);
            // Wrap description manually into two chunks if long
            String desc = s[2];
            int mid = desc.length() > 70 ? desc.lastIndexOf(' ', 68) : desc.length();
            String line1 = desc.substring(0, mid);
            String line2 = mid < desc.length() ? desc.substring(mid + 1) : "";
            txt(cb, line1, tx, yc + 5, 7, Font.NORMAL, INK, ta);
            if (!line2.isEmpty()) txt(cb, line2, tx, yc - 5, 7, Font.NORMAL, INK, ta);
            txt(cb, s[3], tx, yc - 17, 6, Font.ITALIC, ACCENT, ta);
        }

        txt(cb, "All seven steps execute within a single HTTP POST request  ·  No external API calls  ·  In-process only",
            pw/2, 38, 7, Font.ITALIC, MUTED, Element.ALIGN_CENTER);

        doc.close();
        System.out.println("  PDF: Workflow_Diagram.pdf");
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private PdfPTable hRule(Color c, float h, float before, float after) throws DocumentException {
        PdfPTable t = new PdfPTable(1); t.setWidthPercentage(100);
        t.setSpacingBefore(before); t.setSpacingAfter(after);
        PdfPCell cell = new PdfPCell(new Phrase(""));
        cell.setBackgroundColor(c); cell.setBorder(Rectangle.NO_BORDER);
        cell.setFixedHeight(h);
        t.addCell(cell);
        return t;
    }

    private PdfPCell col(float pr, float pt, float pb, float pl) {
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.NO_BORDER);
        c.setPaddingRight(pr); c.setPaddingTop(pt);
        c.setPaddingBottom(pb); c.setPaddingLeft(pl);
        return c;
    }

    private PdfPTable sectionHead(String text) throws DocumentException {
        PdfPTable t = new PdfPTable(1); t.setWidthPercentage(100);
        t.setSpacingBefore(6); t.setSpacingAfter(4);
        PdfPCell c = new PdfPCell();
        c.setBorder(Rectangle.BOTTOM);
        c.setBorderColorBottom(RULE); c.setBorderWidthBottom(0.5f);
        c.setPaddingBottom(3); c.setPaddingLeft(0); c.setPaddingRight(0);
        Paragraph p = new Paragraph();
        p.add(new Chunk(text, FH));
        c.addElement(p);
        t.addCell(c);
        return t;
    }

    private Paragraph bodyLine(String text) {
        Paragraph p = new Paragraph(text, FG);
        p.setSpacingAfter(2.5f); p.setLeading(11);
        return p;
    }

    private Paragraph gap(float h) {
        Paragraph p = new Paragraph(" "); p.setSpacingAfter(h); return p;
    }

    // Canvas primitives
    private void box(PdfContentByte cb, float x, float y, float w, float h,
                     Color fill, Color stroke) {
        cb.setColorFill(fill); cb.setColorStroke(stroke); cb.setLineWidth(0.5f);
        cb.roundRectangle(x, y, w, h, 3); cb.fillStroke();
    }

    private void arrow(PdfContentByte cb, float x1, float y1, float x2, float y2) {
        cb.setColorStroke(MUTED); cb.setColorFill(MUTED); cb.setLineWidth(0.8f);
        cb.moveTo(x1, y1); cb.lineTo(x2, y2); cb.stroke();
        double a = Math.atan2(y2 - y1, x2 - x1);
        float al = 6f;
        cb.moveTo(x2, y2);
        cb.lineTo((float)(x2 - al*Math.cos(a - 0.4)), (float)(y2 - al*Math.sin(a - 0.4)));
        cb.lineTo((float)(x2 - al*Math.cos(a + 0.4)), (float)(y2 - al*Math.sin(a + 0.4)));
        cb.closePath(); cb.fill();
    }

    private void arrowDown(PdfContentByte cb, float x, float fromY, float toY) {
        arrow(cb, x, fromY, x, toY);
    }

    private void txt(PdfContentByte cb, String text, float x, float y,
                     int sz, int style, Color c, int align) {
        cb.beginText();
        cb.setColorFill(c);
        BaseFont bf;
        try {
            bf = style == Font.BOLD   ? BaseFont.createFont(BaseFont.HELVETICA_BOLD,   BaseFont.CP1252, false)
               : style == Font.ITALIC ? BaseFont.createFont(BaseFont.HELVETICA_OBLIQUE,BaseFont.CP1252, false)
               :                        BaseFont.createFont(BaseFont.HELVETICA,        BaseFont.CP1252, false);
        } catch (Exception e) {
            cb.endText(); return;
        }
        cb.setFontAndSize(bf, sz);
        cb.showTextAligned(align, text, x, y, 0);
        cb.endText();
    }

    // ── Page footer ───────────────────────────────────────────────────────────
    static class Footer extends PdfPageEventHelper {
        @Override public void onEndPage(PdfWriter w, Document d) {
            try {
                PdfContentByte cb = w.getDirectContent();
                float pw = d.getPageSize().getWidth(), bot = d.bottomMargin();
                cb.setColorStroke(new Color(226,232,240)); cb.setLineWidth(0.4f);
                cb.moveTo(d.leftMargin(), bot + 14);
                cb.lineTo(pw - d.rightMargin(), bot + 14); cb.stroke();
                Font f = new Font(Font.HELVETICA, 6, Font.NORMAL, new Color(148,163,184));
                ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase("Candidate Transformer AI  \u00B7  Design Document", f),
                    d.leftMargin(), bot + 5, 0);
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Page " + w.getPageNumber(), f),
                    pw - d.rightMargin(), bot + 5, 0);
            } catch (Exception ignored) {}
        }
    }
}

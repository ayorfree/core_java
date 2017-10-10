package com.starfucker_inc.v2ch07.book;

/**
 * @author ayorfree
 * @create 2017-10-10-下午4:24
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.*;
import javax.print.attribute.*;
import javax.swing.*;

/**
 * This frame has a text field for the banner text and buttons for printing, page setup, and print
 * preview.
 */
public class BookTestFrame extends JFrame
{
    private JTextField text;
    private PageFormat pageFormat;
    private PrintRequestAttributeSet attributes;

    public BookTestFrame()
    {
        text = new JTextField();
        add(text, BorderLayout.NORTH);

        attributes = new HashPrintRequestAttributeSet();

        JPanel buttonPanel = new JPanel();

        JButton printButton = new JButton("Print");
        buttonPanel.add(printButton);
        printButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                try
                {
                    PrinterJob job = PrinterJob.getPrinterJob();
                    job.setPageable(makeBook());
                    if (job.printDialog(attributes))
                    {
                        job.print(attributes);
                    }
                }
                catch (PrinterException e)
                {
                    JOptionPane.showMessageDialog(BookTestFrame.this, e);
                }
            }
        });

        JButton pageSetupButton = new JButton("Page setup");
        buttonPanel.add(pageSetupButton);
        pageSetupButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                PrinterJob job = PrinterJob.getPrinterJob();
                pageFormat = job.pageDialog(attributes);
            }
        });

        JButton printPreviewButton = new JButton("Print preview");
        buttonPanel.add(printPreviewButton);
        printPreviewButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                PrintPreviewDialog dialog = new PrintPreviewDialog(makeBook());
                dialog.setVisible(true);
            }
        });

        add(buttonPanel, BorderLayout.SOUTH);
        pack();
    }

    /**
     * Makes a book that contains a cover page and the pages for the banner.
     */
    public Book makeBook()
    {
        if (pageFormat == null)
        {
            PrinterJob job = PrinterJob.getPrinterJob();
            pageFormat = job.defaultPage();
        }
        Book book = new Book();
        String message = text.getText();
        Banner banner = new Banner(message);
        int pageCount = banner.getPageCount((Graphics2D) getGraphics(), pageFormat);
        book.append(new CoverPage(message + " (" + pageCount + " pages)"), pageFormat);
        book.append(banner, pageFormat, pageCount);
        return book;
    }
}

class Banner implements Printable
{
    private String message;
    private double scale;

    /**
     * Constructs a banner.
     * @param m the message string
     */
    public Banner(String m)
    {
        message = m;
    }

    /**
     * Gets the page count of this section.
     * @param g2 the graphics context
     * @param pf the page format
     * @return the number of pages needed
     */
    public int getPageCount(Graphics2D g2, PageFormat pf)
    {
        if (message.equals("")) return 0;
        FontRenderContext context = g2.getFontRenderContext();
        Font f = new Font("Serif", Font.PLAIN, 72);
        Rectangle2D bounds = f.getStringBounds(message, context);
        scale = pf.getImageableHeight() / bounds.getHeight();
        double width = scale * bounds.getWidth();
        int pages = (int) Math.ceil(width / pf.getImageableWidth());
        return pages;
    }

    public int print(Graphics g, PageFormat pf, int page) throws PrinterException
    {
        Graphics2D g2 = (Graphics2D) g;
        if (page > getPageCount(g2, pf)) return Printable.NO_SUCH_PAGE;
        g2.translate(pf.getImageableX(), pf.getImageableY());

        drawPage(g2, pf, page);
        return Printable.PAGE_EXISTS;
    }

    public void drawPage(Graphics2D g2, PageFormat pf, int page)
    {
        if (message.equals("")) return;
        page--; // account for cover page

        drawCropMarks(g2, pf);
        g2.clip(new Rectangle2D.Double(0, 0, pf.getImageableWidth(), pf.getImageableHeight()));
        g2.translate(-page * pf.getImageableWidth(), 0);
        g2.scale(scale, scale);
        FontRenderContext context = g2.getFontRenderContext();
        Font f = new Font("Serif", Font.PLAIN, 72);
        TextLayout layout = new TextLayout(message, f, context);
        AffineTransform transform = AffineTransform.getTranslateInstance(0, layout.getAscent());
        Shape outline = layout.getOutline(transform);
        g2.draw(outline);
    }

    /**
     * Draws 1/2" crop marks in the corners of the page.
     * @param g2 the graphics context
     * @param pf the page format
     */
    public void drawCropMarks(Graphics2D g2, PageFormat pf)
    {
        final double C = 36; // crop mark length = 1/2 inch
        double w = pf.getImageableWidth();
        double h = pf.getImageableHeight();
        g2.draw(new Line2D.Double(0, 0, 0, C));
        g2.draw(new Line2D.Double(0, 0, C, 0));
        g2.draw(new Line2D.Double(w, 0, w, C));
        g2.draw(new Line2D.Double(w, 0, w - C, 0));
        g2.draw(new Line2D.Double(0, h, 0, h - C));
        g2.draw(new Line2D.Double(0, h, C, h));
        g2.draw(new Line2D.Double(w, h, w, h - C));
        g2.draw(new Line2D.Double(w, h, w - C, h));
    }
}

/**
 * This class prints a cover page with a title.
 */
class CoverPage implements Printable
{
    private String title;

    /**
     * Constructs a cover page.
     * @param t the title
     */
    public CoverPage(String t)
    {
        title = t;
    }

    public int print(Graphics g, PageFormat pf, int page) throws PrinterException
    {
        if (page >= 1) return Printable.NO_SUCH_PAGE;
        Graphics2D g2 = (Graphics2D) g;
        g2.setPaint(Color.black);
        g2.translate(pf.getImageableX(), pf.getImageableY());
        FontRenderContext context = g2.getFontRenderContext();
        Font f = g2.getFont();
        TextLayout layout = new TextLayout(title, f, context);
        float ascent = layout.getAscent();
        g2.drawString(title, 0, ascent);
        return Printable.PAGE_EXISTS;
    }
}

class BookTest
{
    public static void main(String[] args)
    {
        EventQueue.invokeLater(new Runnable()
        {
            public void run()
            {
                JFrame frame = new BookTestFrame();
                frame.setTitle("BookTest");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setVisible(true);
            }
        });
    }
}

class PrintPreviewCanvas extends JComponent
{
    private Book book;
    private int currentPage;

    /**
     * Constructs a print preview canvas.
     * @param b the book to be previewed
     */
    public PrintPreviewCanvas(Book b)
    {
        book = b;
        currentPage = 0;
    }

    public void paintComponent(Graphics g)
    {
        Graphics2D g2 = (Graphics2D) g;
        PageFormat pageFormat = book.getPageFormat(currentPage);

        double xoff; // x offset of page start in window
        double yoff; // y offset of page start in window
        double scale; // scale factor to fit page in window
        double px = pageFormat.getWidth();
        double py = pageFormat.getHeight();
        double sx = getWidth() - 1;
        double sy = getHeight() - 1;
        if (px / py < sx / sy) // center horizontally
        {
            scale = sy / py;
            xoff = 0.5 * (sx - scale * px);
            yoff = 0;
        }
        else
        // center vertically
        {
            scale = sx / px;
            xoff = 0;
            yoff = 0.5 * (sy - scale * py);
        }
        g2.translate((float) xoff, (float) yoff);
        g2.scale((float) scale, (float) scale);

        // draw page outline (ignoring margins)
        Rectangle2D page = new Rectangle2D.Double(0, 0, px, py);
        g2.setPaint(Color.white);
        g2.fill(page);
        g2.setPaint(Color.black);
        g2.draw(page);

        Printable printable = book.getPrintable(currentPage);
        try
        {
            printable.print(g2, pageFormat, currentPage);
        }
        catch (PrinterException e)
        {
            g2.draw(new Line2D.Double(0, 0, px, py));
            g2.draw(new Line2D.Double(px, 0, 0, py));
        }
    }

    /**
     * Flip the book by the given number of pages.
     * @param by the number of pages to flip by. Negative values flip backwards.
     */
    public void flipPage(int by)
    {
        int newPage = currentPage + by;
        if (0 <= newPage && newPage < book.getNumberOfPages())
        {
            currentPage = newPage;
            repaint();
        }
    }
}

class PrintPreviewDialog extends JDialog
{
    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;

    private PrintPreviewCanvas canvas;

    /**
     * Constructs a print preview dialog.
     * @param p a Printable
     * @param pf the page format
     * @param pages the number of pages in p
     */
    public PrintPreviewDialog(Printable p, PageFormat pf, int pages)
    {
        Book book = new Book();
        book.append(p, pf, pages);
        layoutUI(book);
    }

    /**
     * Constructs a print preview dialog.
     * @param b a Book
     */
    public PrintPreviewDialog(Book b)
    {
        layoutUI(b);
    }

    /**
     * Lays out the UI of the dialog.
     * @param book the book to be previewed
     */
    public void layoutUI(Book book)
    {
        setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        canvas = new PrintPreviewCanvas(book);
        add(canvas, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();

        JButton nextButton = new JButton("Next");
        buttonPanel.add(nextButton);
        nextButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                canvas.flipPage(1);
            }
        });

        JButton previousButton = new JButton("Previous");
        buttonPanel.add(previousButton);
        previousButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                canvas.flipPage(-1);
            }
        });

        JButton closeButton = new JButton("Close");
        buttonPanel.add(closeButton);
        closeButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent event)
            {
                setVisible(false);
            }
        });

        add(buttonPanel, BorderLayout.SOUTH);
    }
}

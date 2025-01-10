# Install Steps
1. clone xslt repo
2. install apache ant 
3. build the repo with apache ant using `ant`
4. install java (slim jre)
5. Install saxon HE as xslt processesor
6
7. Install fop
   1. Dowload binary from website
   2.  z


# Requirements
- xrechnung / zugferd input
  - Drag and drop
  - from file system
  - optional: from outlook, with send to or something

- Transformation of xrechnung / zugpferd
  - Convert to intermediate xml with saxon HE
  - generate PDF with github tool / fop 

- Display of invoice
  - display pdf embedded into webpage

- Front end buttons
  - View -> Open large window of pdf
  - save -> Stores original input and generate pdf in specific directory
    - starting by year 2025/
    - followed by company name (parsed from xrechnung)
    - file names are invoice referenc number 
    - result: 2025/baywa/590483.pdf
  - print -> Send pdf directly to the configured network printer

- Additional features
  - Front end tab for searching invoices by year, company name, invoice number, date, ... sum , payed date
  - maybe use a lightweight DB for that, that stores file paths and meta information
  - add checkbox if payed or not, maybe provide field for transaction number

- Open questions:
  - what to do if changes to invoices are necessary
  - what to do with non e-rechnung
    - PDF als input ins tool erlauben
    - parse pdf and pre fill fields

# Formats
- xrechnung and zugferd are formats
- ubl and cii are technical xml standards they are based on
- ubl allows other stuff than invoices
- xrechnung: can be based on either ubl or cii
- zugferd: only cii

    ZUGFeRD was developed by a German initiative as a standard for electronic invoices (https://www.ferd-net.de/).
    ZUGFeRD 2.1 is identical to the German/French cooperation Factur-X (ZUGFeRD 2.1 = Factur-X 1.0) (https://www.ferd-net.de/en/standards/zugferd/factur-x).
    The standard Factur-X 1.0 (respectively ZUGFeRD 2.1) is conform with the European norm EN 16931.
    EN 16931 in turn is based on worldwide UN/CEFACT standard 'Cross Industry Invoice' (CII).
    XRechnung as another German standard is a subset of EN 16931. It is defined by another party called KoSIT (https://www.xoev.de/). It comes with its own validation rules (https://xeinkauf.de/dokumente/).
    This means that both Factur-X 1.0 (respectively ZUGFeRD 2.1) and XRechnung are conform with EN 16931. This does not automatically result that those invoices are per se identical.
    To achieve compatibility, ZUGFeRD 2.1.1 introduced a XRechnung reference profile to guarantee compatibility between the two sister formats.


# Tool zum PDF zerlegen

```
java -jar saxon-he-12.5.jar -s:xrechnung-visualization/src/test/instances/maxRechnung_creditnote.xml  -xsl:xrechnung-visualization/src/xsl/ubl-invoice-xr.xsl -o:output.xr
```

```
java -jar saxon-he-12.5.jar -s:ibis.xml -xsl:xrechnung-visualization/src/xsl/xr-pdf.xsl -o:output.fo
```



```
java -jar saxon-he-12.5.jar -s:xrechnung-visualization/src/test/instances/maxRechnung_creditnote.xml -xsl:xrechnung-visualization/src/xsl/xr-pdf.xsl -o:output.fo

```


chmod fop
```
fop-2.10/fop/fop -fo output.fo -pdf output.pdf

```
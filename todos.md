- XML Validation
- Tests
- Frontend changes
- db anbindung
- Normal pdfs

Do not expose publicly because:
- Backend hosts temp files non securely



# Todo backend
- Refactor config and constants
- Improve error handling and propagation to front end
- implement validation step
- test & fix duplicate detection, maybe use hash


# Todo frontend
- add finish/ clear button
- clear when click on import
- improve import page styling (also use auto scroll)
- add validation section with either green or error or warning
- add darkmode
- dashboard
  - add button to open invoice

Environment vars:
- Backend
  - Hostname, Port server
  - Ggf. db access
  - Transformer Config
    - Language
    - Sonstiges
  - Output Path 
- Frontend
  - Hostname, Port
  - language


# Use Cases / Endpoints

## Errors:
[err001]: File is neither PDF nor XML
[err002]: PDF File containes XML, but the XML is not a valid e-invoice, conversion not possible, invoice should be denied
[err003]: XML is not a valid e-invoice, conversion not possible, invoice should be denied

## Warning:
{warn001}: PDF File appears to be ZUGfeRD/Factur-X, but the contained XML has slight deviations from the standard
{warn002}: XML File appears to be XRechnung, but the XML has slight deviatoins from the standards

## Endpints
- /upload
    - Upload any PDF / XML (Deny [1]}

    - Validate file
      - PDF
        - Is ZUGfeRD?
          - Contains XML -> ValidateXml ? Accept / Deny [2]{1}
        - Not ZUGfeRD
      - XML
        - ValidateXml -> Accept (2) / Deny [3]
    
    - temp DB entry
    - Returns 200, {invoiceId}, {invoiceMeta}, {KeyInformation}, {TempFileUrl}

- /persist/{invoiceId}
    - Save original and transformed file to system
    - Create DB Entry for persisted file
    - Return 200 OK

- /print/{invoiceId}
  - Send print command to network printer

## Internal methods
- setDeletionTime(invoiceId):
    - after 1 day
    - Remove temp DB entry
    - Remove temp files

- validateXml(invoiceId):
    - Run external validator

- parseXml
    - return keyInformation
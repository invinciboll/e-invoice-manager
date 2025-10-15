# Clone with submodules
```bash
git clone --recurse-submodules --remote-submodules https://github.com/invinciboll/e-invoice-manager.git
```

Download XRechnung validdator xsl und Szenario
```bash
curl -L "https://github.com/itplr-kosit/validator-configuration-xrechnung/releases/download/release-2025-03-21/validator-configuration-xrechnung_3.0.2_2025-03-21.zip" --output validator-configuration.zip && unzip validator-configuration.zip -d backend/validator-configuration-xrechnung && rm validator-configuration.zip
```

```bash
git clone https://github.com/LandrixSoftware/validator-configuration-zugferd.git
```



Merge beide varianten
```bash
curl -L "https://github.com/itplr-kosit/validator-configuration-xrechnung/releases/download/release-2025-03-21/validator-configuration-xrechnung_3.0.2_2025-03-21.zip" --output validator-configuration.zip && unzip validator-configuration.zip -d validator-configuration-xrechnung && rm validator-configuration.zip
```

```bash
git clone https://github.com/LandrixSoftware/validator-configuration-zugferd.git
```

```bash


<!-- Download Factur-X validator xsl
```bash
curl -L https://www.ferd-net.de/fileadmin/user_upload/FeRD/Downloads/ZF233_DE_01.zip --output zf_data.zip && unzip zf_data.zip -d zf_data && mv zf_data/Schema backend/validator-configuration-xrechnung/resources/factur-x && rm -r zf_data && rm zf_data.zip
``` -->

Factur-X Szenarien müssen eingefügt werden



```bash
curl -L "https://github.com/itplr-kosit/validator/releases/download/v1.5.2/validator-1.5.2.zip" --output validator.zip && unzip validator.zip -d backend/validator && rm validator.zip && mvn install:install-file -Dfile=backend/validator/validator-1.5.2-standalone.jar -DgroupId=org.kosit -DartifactId=validator -Dversion=1.5.2 -Dclassifier=standalone -Dpackaging=jar
```

# KoSIT Submodules
This project uses KoSIT projects, to validate and visualize e-invoices. The following projects are used as git submodules:

- [xrechnung-visualization](https://github.com/itplr-kosit/xrechnung-visualization.git) (Apache 2.0 License)




### Run Dev Env
`mvn spring-boot:run`

`mvn test`

`npm run dev`

# Techstack
## Backend
- Java
- Spring
- Maven
- Saxon-HE, FOP, H2

## Frontend
- Vite
- React
- Typescript + SWE
- ShadCN + Tailwind + Heroicons





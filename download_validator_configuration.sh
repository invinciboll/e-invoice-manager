#!/bin/bash
# retrieve_validator_confdownload_validator_configurationiguration.sh

curl -L "https://github.com/itplr-kosit/validator-configuration-xrechnung/releases/download/release-2025-03-21/validator-configuration-xrechnung_3.0.2_2025-03-21.zip" --output validator-configuration.zip
unzip validator-configuration.zip -d backend/validator-configuration-xrechnung
rm validator-configuration.zip

cd backend
git clone https://github.com/LandrixSoftware/validator-configuration-zugferd.git

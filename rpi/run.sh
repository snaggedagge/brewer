#!/bin/bash
# To activate additional one wire
sudo dtoverlay w1-gpio gpiopin=27 pullup=0

java -jar /var/opt/brewer/brewer-1.0.jar

echo "Will shutdown in 60 seconds due to failure"
sleep 60
reboot
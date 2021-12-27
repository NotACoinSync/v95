@echo off
@title MeoStory Ch1
set CLASSPATH=.;dist\*
java -Djavax.net.ssl.keyStore=vertisykey.jks -Djavax.net.ssl.keyStorePassword=papapapakaka -Djavax.net.ssl.trustStore=vertisykey.jks -Djavax.net.ssl.trustStorePassword=papapapakaka -Dwzpath=wz\ net.channel.ChannelServer 0 0 1 2 3 4 5
pause
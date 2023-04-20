FROM liferay/jar-runner:latest

COPY *.jar /opt/liferay/jar-runner.jar
COPY *.sh /usr/local/bin/

USER root

RUN curl -fsSL https://deb.nodesource.com/setup_lts.x | bash - && \
	apt-get install --yes git nodejs p7zip-full rsync && \
	npm install --global generator-liferay-fragments generator-liferay-theme yarn yo && \
	mkdir /public_html

USER liferay
FROM java:8-jdk
ENV _JAVA_OPTIONS -Dc2mon.server.elasticsearch.home=/c2mon-server/elasticsearch
WORKDIR /c2mon-server
ADD c2mon-server /c2mon-server
RUN chmod 755 /c2mon-server/bin/*
EXPOSE 1099 61616 9001 9200
CMD ["bin/c2mon.sh","run"]
VOLUME /c2mon-server/log
VOLUME /c2mon-server/conf
VOLUME /c2mon-server/elasticsearch

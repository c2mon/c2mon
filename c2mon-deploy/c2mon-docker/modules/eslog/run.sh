docker run --rm --name eslog -ti -p 9200:9200 -p 9300:9300 elasticsearch:2.1.0 -Des.network.bind_host=0.0.0.0 -Des.cluster.name=c2mon -Des.node.master=true 

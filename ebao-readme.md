=== 运行 hbase
```
mkdir -p data/hbase
docker rm -f hbase-docker \
docker run -d \
--name=hbase-docker \
-h hbase-docker \
-v $PWD/data/hbase:/data \
-p 16010:16010 \
-p 9095:9095 \
-p 8085:8085 \
-p 2181:2181 \
docker-all.repo.ebaotech.com/dajobe/hbase
```
=== 初始化数据
```
docker run \
--rm \
-it \
-v /home/ebaocloud/hbase:/data \
--link hbase-docker:hbase-docker \
docker-all.repo.ebaotech.com/dajobe/hbase hbase shell /data/init-hbase.txt
```

=== 运行pinpoint collector

```
docker run -d \
--name pinpoint-collector \
-p 9994:9994
-p 9995:9995
-p 9996:9996
docker-all.repo.ebaotech.com/ebaocloud/pinpoint-collector:1.7.0-eb-SNAPSHOT
```
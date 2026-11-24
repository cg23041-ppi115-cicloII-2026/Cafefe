FROM debian:stable

USER root

RUN apt update && apt install -y openjdk-21-jdk unzip wget curl telnet iproute2 && useradd -m -s /bin/bash usuario

USER usuario
WORKDIR /home/usuario

RUN wget https://public.dhe.ibm.com/ibmdl/export/pub/software/openliberty/runtime/release/26.0.0.8/openliberty-jakartaee11-26.0.0.8.zip \
    && unzip openliberty-jakartaee11-26.0.0.8.zip \
    && wget -O /home/usuario/wlp/lib/postgresql-42.7.3.jar https://jdbc.postgresql.org/download/postgresql-42.7.3.jar \
    && wget -O /home/usuario/wlp/lib/primefaces-15.0.0-jakarta.jar https://repo1.maven.org/maven2/org/primefaces/primefaces/15.0.0/primefaces-15.0.0-jakarta.jar \
    && /home/usuario/wlp/bin/server create app --template=jakartaee11 \
    && mkdir -p /home/usuario/wlp/usr/servers/app/configDropins/defaults \
    && echo '<?xml version="1.0"?><server description="Driver JDBC para Postgres"><jdbcDriver id="postgresql-driver" javax.sql.DataSource="org.postgresql.ds.PGSimpleDataSource" javax.sql.ConnectionPoolDataSource="org.postgresql.ds.PGConnectionPoolDataSource" libraryRef="postgresql-library"/><library id="postgresql-library"><fileset id="PostgresqlFileSet" dir="${wlp.install.dir}/lib" includes="postgresql-42.7.3.jar"/></library></server>' > /home/usuario/wlp/usr/servers/app/configDropins/defaults/postgres-driver.xml \
    && echo '<?xml version="1.0" encoding ="UTF-8"?><server description="OpenLiberty Jakarta EE 11"><library id="global"><fileset id="PrimefacesFileset" dir="${wlp.install.dir}/lib" includes="primefaces-15.0.0-jakarta.jar"/></library></server>' > /home/usuario/wlp/usr/servers/app/configDropins/defaults/primefaces.xml \
    && echo '<?xml version="1.0" encoding="UTF-8"?><server description="new server"><featureManager><feature>jakartaee-11.0</feature><feature>microProfile-7.1</feature></featureManager><basicRegistry id="basic" realm="BasicRealm"></basicRegistry><httpEndpoint id="defaultHttpEndpoint" host="0.0.0.0" httpPort="9080" httpsPort="9443"/><applicationManager autoExpand="true"/><ssl id="defaultSSLConfig" trustDefaultCerts="true"/><dataSource id="CafefeDataSource" jndiName="jdbc/pgdb" jdbcDriverRef="postgresql-driver" type="javax.sql.ConnectionPoolDataSource" transactional="true"><properties serverName="${PG_HOST}" databaseName="${PG_DB}" portNumber="${PG_PORT}" user="${PG_USER}" password="${PG_PASS}"/></dataSource></server>' > /home/usuario/wlp/usr/servers/app/server.xml

EXPOSE 9080
CMD ["/home/usuario/wlp/bin/server", "run", "app"]
(defproject elephantdb-deploy "1.1.0-SNAPSHOT"
  :main elephantdb.deploy.provision
  :resources-path "resources"
  :repositories {"sonatype" "https://oss.sonatype.org/content/repositories/releases"}
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/tools.cli "0.1.0"]
                 [org.antlr/stringtemplate "3.2"]]
  :dev-dependencies [[org.cloudhoist/pallet "0.6.6"]
                     [org.cloudhoist/git "0.5.0"]
                     [org.cloudhoist/java "0.5.1"]
                     [org.cloudhoist/ssh-key "0.6.0"]                   
                     [org.cloudhoist/automated-admin-user "0.6.0"]
                     [org.jclouds.provider/aws-ec2 "1.2.1"]
                     [org.jclouds.provider/aws-s3 "1.2.1"]
                     [org.jclouds.driver/jclouds-jsch "1.2.1"]
                     [org.jclouds.driver/jclouds-log4j "1.2.1"]
                     [log4j/log4j "1.2.14"]])

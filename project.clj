(def dev-dependencies '[[org.slf4j/slf4j-log4j12 "1.6.4"]
                        [org.cloudhoist/pallet "0.6.6"]
                        [org.cloudhoist/git "0.5.0"]
                        [org.cloudhoist/java "0.5.1"]
                        [org.cloudhoist/ssh-key "0.6.0"]
                        [org.cloudhoist/automated-admin-user "0.6.0"]
                        [org.jclouds.provider/aws-ec2 "1.2.1"]
                        [org.jclouds.provider/aws-s3 "1.2.1"]
                        [org.jclouds/jclouds-blobstore "1.2.1"]
                        [org.jclouds/jclouds-compute "1.2.1"]
                        [org.jclouds/jclouds-scriptbuilder "1.2.1"]
                        [org.jclouds.driver/jclouds-jsch "1.2.1"]])

(defproject elephantdb-deploy "1.1.0-SNAPSHOT"
  :main elephantdb.deploy.provision

  ;; Lein 1
  :resources-path "resources"
  
  ;; Lein 2
  :resource-paths ["resources"]
  
  :repositories {"sonatype" "https://oss.sonatype.org/content/repositories/releases"}

  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/tools.cli "0.1.0"]
                 [org.antlr/stringtemplate "3.2"]]
  
  ;; Lein 1
  :dev-dependencies ~dev-dependencies

  ;; Lein 2  
  :profiles {
    :dev {
      :dependencies ~dev-dependencies
    }
  })

## Setup

TODO: Replace this with real instructions.

1) Add keys to `~/.ssh/elephantdb` and `~/.ssh/elephantdb.pub`. These will be installed on EDB server.
2) Create `~/.pallet/config.clj` with contents:

          (defpallet
               :services {
              :elephantdb-deploy {
                         :provider "aws-ec2"
                         :identity "XXXX"
                         :credential "XXXXX"
                         }

              :elephantdb-data {
                         :blobstore-provider "aws-s3"
                         :provider "aws-ec2"
                         :identity "XXXX"
                         :credential "XXXX"
                         }
             }
                               {:lift-fn pallet.core/parallel-lift
                   :converge-fn pallet.core/parallel-adjust-node-counts}})


## Usage

To provision a cluster:

```bash
$ lein run --start --ring <ring name>
```

To deploy to existing edb cluster:

```bash
$ lein run --start --ring <ring name>
```

;; Local -- replace the meta with this:

```clojure
{:vmfest-Debian-6.0.2.1-64bit-v0.3 {:uuid "/Users/sritchie/.vmfest/models/vmfest-vmfest-Debian-6.0.2.1-64bit-v0.3.vdi", :os-type-id "Debian_64", :sudo-password "vmfest", :no-sudo false, :username "vmfest", :os-family :debian, :os-version "6.0.2.1", :os-64-bit true, :password "vmfest", :description "Debian 6.0.2.1 (64bit) v0.3"}}
```

Squid conf: https://gist.github.com/1357652

## Notes

Deploy should allow the user to provide the following:

1. :hdfs-conf
2. :blob-conf

If an hdfs-conf is specified, but no blob-conf, the deploy will look in the same location as hdfs-conf for the configuration files, else it'll search more broadly in the blob-conf.

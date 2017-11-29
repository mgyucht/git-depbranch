(defproject git-depbranch "0.1.0-SNAPSHOT"
  :description "Simple inter-dependent branches in Git."
  :url "https://github.com/mgyucht/git-depbranch"
  :license {:name "MIT License"
            :url "http://www.opensource.org/licenses/mit-license.php"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]]
  :main git-depbranch.core
  :bin {:name "git-depbranch"
        :jvm-opts ["-Dfile.encoding=UTF-8"]}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[lein-binplus "0.6.2"]]}})

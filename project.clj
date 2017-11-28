(defproject git-depbranch "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :main git-depbranch.core
  :aot [git-depbranch.core]
  :bin {:name "git-depbranch"
        :bin-path "/Users/miles/dotfiles/bin"
        :jvm-opts ["-Dfile.encoding=UTF-8"]}
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}
             :dev {:plugins [[lein-binplus "0.6.2"]]}})

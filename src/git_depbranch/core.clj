(ns git-depbranch.core
  (:require [clojure.java.shell :as shell]
            [clojure.string :as string]
            [clojure.tools.cli :as cli])
  (:gen-class))

(def BASE-BRANCH-SUFFIX "_db_base")

(defn depbranch-conf-key
  "Return the depbranch config key for the given branch"
  [branch-name]
  (str "branch." branch-name ".depbranch"))

(defn get-current-branch
  "Returns the name of the currently checked-out Git branch, or nil if not in a git repository."
  []
  (let [res (shell/sh "git" "rev-parse" "--abbrev-ref" "HEAD")]
    (when (= 0 (:exit res))
      (string/trim (:out res)))))

(defn parse-depbranch-args
  "Parse args according to command-spec."
  [args command-spec]
  (let [{:keys [options arguments errors]} (cli/parse-opts args command-spec)
        error (cond
                errors (first errors)
                (> (count arguments) 1) (str "Too many arguments: " arguments)
                :else nil)
        branch-name (when-not error (first arguments))
        options (when-not error options)]
    {:branch-name branch-name
     :options options
     :error error}))

(defn print-error-and-exit
  "Prints the provided error and exits with exit code 1."
  [error]
  (println error)
  (System/exit 1))

(def SHOW-SPEC [[nil "--recursive" "Recursively get depbranches" :default false]])

(defn get-depbranches-for-branch
  [branch-name recursive?]
  (let [config-key (depbranch-conf-key branch-name)
        res (shell/sh "git" "config" "--get-all" config-key)
        depbranches (when (= 0 (:exit res))
                      (-> (:out res)
                          string/trim
                          (string/split #"\n")))]
    (if (and depbranches recursive?)
      (concat depbranches
              (mapcat #(get-depbranches-for-branch % recursive?)
                      depbranches))
      depbranches)))

(defn show-depbranches
  "Returns a list of the current branch's dependent branches as a string."
  ([& args]
   (let [{:keys [branch-name options error]} (parse-depbranch-args args SHOW-SPEC)]
     (if error
       (print-error-and-exit error)
       (let [branch-name (or branch-name
                             (get-current-branch))
             recursive? (:recursive options)]
         (->> (get-depbranches-for-branch branch-name recursive?)
             (string/join "\n")))))))

(defn add-depbranch
  "Adds the specified branch as a dependent branch for the current branch."
  [branch-name]
  (let [config-key (depbranch-conf-key (get-current-branch))]
    (shell/sh "git" "config" "--add" config-key branch-name)
    nil))

(defn remove-depbranch
  "Removes the specified branch as a dependent branch for the current branch."
  [branch-name]
  (let [config-key (depbranch-conf-key (get-current-branch))]
    (shell/sh "git" "config" "--unset-all" config-key branch-name)
    nil))

(defn get-base-branch
  "Returns the base branch for the current branch."
  ([] (get-base-branch (get-current-branch)))
  ([branch-name] (str branch-name BASE-BRANCH-SUFFIX)))

(defn update-base-branch
  "Updates the base branch for the current branch."
  ([] (update-base-branch (get-current-branch)))
  ([branch-name]
   (when-let [dep-branches (show-depbranches branch-name)]
     (doseq [branch dep-branches]
       (update-base-branch branch))
     (let [base-branch (get-base-branch branch-name)
           [first-branch & other-branches] dep-branches]
       (shell/sh "git" "checkout" first-branch)
       (shell/sh "git" "checkout" "-B" base-branch)
       (doseq [branch other-branches]
         (let [commit-msg (str "Merging " branch " into " base-branch)]
           (shell/sh "git" "merge" "-m" commit-msg branch)))
       (shell/sh "git" "checkout" branch-name)
       (let [commit-msg (str "Merging " base-branch " into " branch-name)]
         (shell/sh "git" "merge" "-m" commit-msg base-branch))))))

(defn depbranch-dispatch
  [command & _]
  command)

(defmulti depbranch
  "Dispatches to the correct depbranch implementation method."
  depbranch-dispatch)

(defmethod depbranch "show"
  [_ & rest] (apply show-depbranches rest))

(defmethod depbranch "add"
  [_ new-branch] (add-depbranch new-branch))

(defmethod depbranch "remove"
  [_ branch] (remove-depbranch branch))

(defmethod depbranch "base-branch"
  [_ & rest] (apply get-base-branch rest))

(defmethod depbranch "update-base"
  [_ & rest] (apply update-base-branch rest))

(defmethod depbranch :default
  [command & _] (println (str "Unsupported command: " command)))

(def USAGE
  "The usage text for git-depbranch."
  "usage: git depbranch show [--recursive] [<branch-name>]
   or: git depbranch add <branch-name>
   or: git depbranch remove <branch-name>
   or: git depbranch base-branch [<branch-name>]
   or: git depbranch update-base [<branch-name>]

Generic options
    -h, --help  Print this help message")

(def CLI-SPEC [["-h" "--help" "Print the help message" :default false]])

(defn run-depbranch
  "Parse arguments and run git-depbranch."
  [args]
  (let [{:keys [options arguments]} (cli/parse-opts args CLI-SPEC :in-order true)]
    (cond
      (:help options)
      (println USAGE)

      :else
      (when-let [result (apply depbranch arguments)]
        (println result)))))

(defn -main
  "Runs the depbranch utility with the provided args, and prints the result."
  [& args]
  (try
    (run-depbranch args)
    (finally
      (shutdown-agents))))

package com.kylenicholls.stash.parameterizedbuilds.item;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.repository.Branch;
import com.atlassian.bitbucket.repository.RefChange;
import com.atlassian.bitbucket.repository.Repository;
import com.google.common.base.Preconditions;
import java.util.function.Supplier;

import com.kylenicholls.stash.parameterizedbuilds.item.Job.Trigger;

public class BitbucketVariables {
    private Map<String, BitbucketVariable<String>> variables;
    private final String [] SET_VALUES = {
            "$BRANCH", "$COMMIT", "$URL", "$REPOSITORY", "$PROJECT", "$PRID",
            "$PRAUTHOR", "$PREMAIL", "$PRTITLE", "$PRDESCRIPTION", "$PRDESTINATION",
            "$PRURL", "$TRIGGER", "$MERGECOMMIT","$PRSOURCEPROJECT", "$PRSOURCEREPOSITORY",
            "$PRUSERNAME",};
    private final Set<String> allowedVariables = new HashSet<>(Arrays.asList(SET_VALUES));

    private BitbucketVariables(Builder builder){
        assert allowedVariables.containsAll(builder.variables.keySet());
        this.variables = builder.variables;
    }

    public Map<String, BitbucketVariable<String>> getVariables() {
        return variables;
    }

    public String fetch(String key) {
        return variables.get(key).getOrCompute();
    }

    public static class Builder {
        private Map<String, BitbucketVariable<String>> variables;

        public Builder() {
            this.variables = new HashMap<>();
        }

        public Builder add(String key, Supplier<String> supplier) {
            Preconditions.checkNotNull(key);
            if (variables.containsKey(key)) {
                return this;
            }
            BitbucketVariable<String> variable = new BitbucketVariable<>(supplier);
            variables.put(key, variable);
            return this;
        }

        public Builder populateFromPR(PullRequest pullRequest, Repository repository,
                                      String projectKey, Trigger trigger, String url){
            String prId = Long.toString(pullRequest.getId());
            return add("$BRANCH", () -> pullRequest.getFromRef().getDisplayId())
                    .add("$COMMIT", () -> pullRequest.getFromRef().getLatestCommit())
                    .add("$URL", () -> url)
                    .add("$REPOSITORY", repository::getSlug)
                    .add("$PROJECT", () -> projectKey)
                    .add("$PRID", () -> prId)
                    .add("$PRAUTHOR", () -> pullRequest.getAuthor().getUser().getDisplayName())
                    .add("$PREMAIL", () -> pullRequest.getAuthor().getUser().getEmailAddress())
                    .add("$PRUSERNAME", () -> pullRequest.getAuthor().getUser().getName())
                    .add("$PRTITLE", pullRequest::getTitle)
                    .add("$PRDESCRIPTION", pullRequest::getDescription)
                    .add("$PRDESTINATION", () ->  pullRequest.getToRef().getDisplayId())
                    .add("$PRURL", () -> url + "/projects/" + projectKey + "/repos/" +
                            repository.getSlug() + "/pull-requests/" + prId)
                    .add("$PRSOURCEPROJECT",  () -> pullRequest.getFromRef().getRepository()
                            .getProject().getKey())
                    .add("$PRSOURCEREPOSITORY", () -> pullRequest.getFromRef().getRepository()
                            .getSlug())
                    .add("$TRIGGER", trigger::toString);
        }

        public Builder populateFromRef(String branch, RefChange refChange, Repository repository,
                                       String projectKey, Trigger trigger, String url){
            return add("$BRANCH", () -> branch)
                    .add("$COMMIT", refChange::getToHash)
                    .add("$URL", () -> url)
                    .add("$REPOSITORY", repository::getSlug)
                    .add("$PROJECT", () -> projectKey)
                    .add("$TRIGGER",  trigger::toString);
        }

        public Builder populateFromBranch(Branch branch, Repository repository, String projectKey,
                                          Trigger trigger, String url){
            return add("$BRANCH", branch::getDisplayId)
                    .add("$COMMIT", branch::getLatestCommit)
                    .add("$URL", () -> url)
                    .add("$REPOSITORY", repository::getSlug)
                    .add("$PROJECT", () -> projectKey)
                    .add("$TRIGGER", trigger::toString);
        }

        public Builder populateFromStrings(String branch, String commit, Repository repository,
                                           String projectKey, Trigger trigger, String url){
            return add("$BRANCH", () -> branch)
                    .add("$COMMIT", () -> commit)
                    .add("$URL", () -> url)
                    .add("$REPOSITORY", repository::getSlug)
                    .add("$PROJECT", () -> projectKey)
                    .add("$TRIGGER", trigger::toString);
        }

        public BitbucketVariables build() {
            return new BitbucketVariables(this);
        }
    }
}

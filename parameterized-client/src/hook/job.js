import React from 'react';
import { connect } from 'react-redux';

const GenericField = ({
                        jobInfo,
                        id,
                        errors,
                        updateText,
                        fieldName,
                        fieldLabel,
                        required,
                        description = ""
                      }) => {
    return (
        <div className={"field-group" + (jobInfo.active ? "" : " hidden") }>
            <label htmlFor={fieldName + "-" + id}>
                {fieldLabel}
                {required && <span className={"aui-icon icon-required"}/>}
            </label>
            <input id={fieldName + "-" + id} className={"text"} name={fieldName + "-" + id} value={jobInfo[fieldName]}
                   type={"text"} onChange={e => {updateText(id, fieldName, e.target.value)}}/>
            {description.length > 0 && <div className={"description"}>{description}</div>}
            {typeof errors[fieldName + "-" + id] !== 'undefined' &&
            <div className={"error"}>{errors[fieldName + "-" + id]}</div>}
        </div>
    )
};

const TriggerButton = ({
                           triggerClass,
                           description,
                           triggerId,
                           triggerText,
                           id,
                           jobInfo,
                           updateTrigger,
                       }) => {
    return (
        <a className={triggerClass} href={"#"} title={description}
           onClick={() => {updateTrigger(id, triggerId)}}>
            <span className={"aui-lozenge" + (jobInfo.triggers.includes(triggerId) ? " aui-lozenge-success" : "")}>{triggerText}</span>
        </a>
    )
};

const OptionalTextField = ({
                            requiredTriggers,
                            fieldName,
                            fieldLabel,
                            description,
                            id,
                            jobInfo,
                            errors,
                            updateText,
                           }) => {
    let display = jobInfo.active && (requiredTriggers.some(trigger => jobInfo.triggers.includes(trigger)));
    return (
        <div className={"field-group" + (display ? "" : " hidden")}>
            <label htmlFor={fieldName + "-" + id}>{fieldLabel}</label>
            <input id={fieldName + "-" + id} className={"text full-width-field"} name={fieldName + "-" + id} type={"text"}
                   value={jobInfo[fieldName]} onChange={(e) => {updateText(id, fieldName, e.target.value)}}/>
            <div className={"description"}>
                {description}
            </div>
            {typeof errors[fieldName + "-" + id] !== 'undefined' &&
                <div className={"error"}>{errors[fieldName + "-" + id]}</div>}
        </div>
    )
};

const JobContainer = ({
    jobInfo,
    errors,
    id,
    jenkinsServers,
    children,
    toggleJob,
    deleteJob,
    updateText,
    updateTrigger
}) => {
    let serverValues = jenkinsServers == null ? []: jenkinsServers;
    let serverOptions = [<option value={""}>Choose an option</option>];
    serverValues.forEach(server => {
        let serverPrefix = server["alias"] || server["url"];
        let serverText = serverPrefix + " (" + server["scope"] + ")";
        let serverValue = server["scope"] == "project" ? server["project"] : "global-settings" ;
        serverOptions.push(<option value={serverValue}>{serverText}</option>)
    });

    let jenkinsErrors;
    if (jenkinsServers == null || jenkinsServers.length == 0) {
        jenkinsErrors = <div className={"error"}>{"No jenkins servers are defined"}</div>
    } else if (errors["jenkinsServer-" + id] !== 'undefined'){
        jenkinsErrors = <div className={"error"}>{errors["jenkinsServer-" + id]}</div>
    }

    return (
        <div id={"job-" + id}>
            <div className={"delete-job inline-button"}>
                <a href={"#"} title={"Delete job"} onClick={e => {e.preventDefault(); deleteJob(id);}}>
                    <span className={"aui-icon aui-icon-small aui-iconfont-remove"}/>
                </a>
            </div>
            <div className={"toggle-job inline-button"}>
                <a href="#" title="Toggle job details" onClick={e => {e.preventDefault(); toggleJob(id);}}>
                    <span className={"aui-icon aui-icon-small " + (jobInfo.active ? "aui-iconfont-expanded" : "aui-iconfont-collapsed")}/>
                    {jobInfo.jobName}
                </a>
            </div>
            <GenericField jobInfo={jobInfo}  id={id} errors={errors} updateText={updateText} fieldName={"jobName"} fieldLabel={"Job Name"} required={true}/>
            <div className={"field-group" + (jobInfo.active ? "" : " hidden") }>
                <label htmlFor={"jenkinsServer-" + id}>Jenkins Server <span className={"aui-icon icon-required"}/></label>
                <select id={"jenkinsServer-" + id} className={"select"} name={"jenkinsServer-" + id} value={jobInfo.jenkinsServer}
                        onChange={e => {updateText(id, 'jenkinsServer', e.target.value)}}
                        disabled={jenkinsServers == null || jenkinsServers.length == 0}>
                    {serverOptions}
                </select>
                {jenkinsErrors}
            </div>
            <div className={"field-group" + (jobInfo.active ? "" : " hidden") }>
                <label htmlFor={"isTag-" + id}>Ref Type</label>
                <select id={"isTag-" + id} className={"select"} name={"isTag-" + id} value={jobInfo.isTag}
                        onChange={e => {updateText(id, 'isTag', e.target.value)}}>
                    <option value={false}>branch</option>
                    <option value={true}>tag</option>
                </select>
            </div>
            <fieldset className={"group field-group" + (jobInfo.active ? "" : " hidden") }>
                <legend><span>Multibranch Pipeline</span></legend>
                <div className={"checkbox"}>
                    <input id={"isPipeline-" + id} className={"checkbox"} name={"isPipeline-" + id} checked={jobInfo.isPipeline}
                           type={"checkbox"} onClick={() => {updateText(id, 'isPipeline', !jobInfo.isPipeline)}}/>
                </div>
            </fieldset>
            <div className={"field-group" + (jobInfo.active ? "" : " hidden")}>
                <label htmlFor={"trigger-buttons-" + id}>
                    Triggers
                    <span className={"aui-icon icon-required"}/>
                </label>
                <TriggerButton triggerClass={"branch-created"} description={"Triggers when new branches or tags are created"}
                               triggerId={"add;"} triggerText={"Ref Created"} id={id} jobInfo={jobInfo} updateTrigger={updateTrigger}/>&nbsp;
                <TriggerButton triggerClass={"push-event"} description={"Triggers on branch push events"}
                               triggerId={"push;"} triggerText={"Push Event"} id={id} jobInfo={jobInfo} updateTrigger={updateTrigger}/>&nbsp;
                <TriggerButton triggerClass={"manual"} description={"Adds a build button to the branch actions menu"}
                               triggerId={"manual;"} triggerText={"Manual"} id={id} jobInfo={jobInfo} updateTrigger={updateTrigger}/><br/>
                <TriggerButton triggerClass={"branch-deleted"} description={"Triggers when a branch or tag is deleted"}
                               triggerId={"delete;"} triggerText={"Ref Deleted"} id={id} jobInfo={jobInfo} updateTrigger={updateTrigger}/>&nbsp;
                <TriggerButton triggerClass={"pr-auto-merged"} description={"Triggers when a branch is merged via Bitbucket's Automatic Merge feature"}
                               triggerId={"prautomerged;"} triggerText={"Auto Merged"} id={id} jobInfo={jobInfo} updateTrigger={updateTrigger}/>&nbsp;
                <TriggerButton triggerClass={"pr-opened"} description={"Triggers when a pull request is opened"}
                               triggerId={"propened;"} triggerText={"PR Opened"} id={id} jobInfo={jobInfo} updateTrigger={updateTrigger}/><br/>
                <TriggerButton triggerClass={"pr-reopened"} description={"Triggers when a pull request is re-opened"}
                               triggerId={"prreopened;"} triggerText={"PR Reopened"} id={id} jobInfo={jobInfo} updateTrigger={updateTrigger}/>&nbsp;
                <TriggerButton triggerClass={"pr-source-rescoped"} description={"Triggers when a pull request is rescoped on source branch"}
                               triggerId={"prsourcerescoped;"} triggerText={"PR source rescoped"} id={id} jobInfo={jobInfo} updateTrigger={updateTrigger}/><br/>
                <TriggerButton triggerClass={"pr-dest-rescoped"} description={"Triggers when a pull request is rescoped on destination branch"}
                               triggerId={"prdestrescoped;"} triggerText={"PR dest rescoped"} id={id} jobInfo={jobInfo} updateTrigger={updateTrigger}/>&nbsp;
                <TriggerButton triggerClass={"pr-merged"} description={"Triggers when a pull request is merged"}
                               triggerId={"prmerged;"} triggerText={"PR Merged"} id={id} jobInfo={jobInfo} updateTrigger={updateTrigger}/>&nbsp;
                <TriggerButton triggerClass={"pr-declined"} description={"Triggers when a pull request is declined"}
                               triggerId={"prdeclined;"} triggerText={"PR Declined"} id={id} jobInfo={jobInfo} updateTrigger={updateTrigger}/><br/>
                <TriggerButton triggerClass={"pr-deleted"} description={"Triggers when a pull request is deleted"}
                               triggerId={"prdeleted;"} triggerText={"PR Deleted"} id={id} jobInfo={jobInfo} updateTrigger={updateTrigger}/>&nbsp;
                <TriggerButton triggerClass={"pr-approved"} description={"Triggers when a pull request is approved"}
                               triggerId={"prapproved;"} triggerText={"PR Approved"} id={id} jobInfo={jobInfo} updateTrigger={updateTrigger}/>&nbsp;
                {typeof errors["triggers-" + id] !== 'undefined' &&
                    <div className={"error"}>{errors["triggers-" + id]}</div>}
            </div>
            <div className={"field-group hidden"}>
                <label htmlFor={"triggers-" + id}>Triggers</label>
                <input id={"triggers-" + id} className={"text"} name={"triggers-" + id}
                       type={"text"} value={jobInfo.triggers}/>
            </div>
            <GenericField jobInfo={jobInfo} id={id} errors={errors} updateText={updateText} fieldName={"token"} fieldLabel={"Token"} required={false}
                description={"Trigger builds remotely (e.g., from scripts) or leave blank to use user API token"}/>
            <div className={"field-group" + (jobInfo.active ? "" : " hidden")}>
                <label htmlFor={"buildParameters-" + id}>Build Parameters</label>
                <textarea id={"buildParameters-" + id} className={"textarea full-width-field"} name={"buildParameters-" + id}
                          value={jobInfo.buildParameters} rows={3} onChange={e => {updateText(id, 'buildParameters', e.target.value)}}/>
                <div className={"description"}>
                    {"Key=Value pairs separated by new line. For choice parameters separate values with a semicolon. " +
                     "Available Bitbucket variables: $BRANCH, $COMMIT, $REPOSITORY, $PROJECT, $TRIGGER (for PR triggers " +
                     "also $PRID, $PRTITLE, $PRDESTINATION, $PRAUTHOR, $PREMAIL, $PRDESCRIPTION, $PRURL, $PRUSERRNAME, $PRSOURCEPROJECT, $PRSOURCEREPOSITORY, and for PR MERGED triggers " +
                     "$MERGECOMMIT))"}
                </div>
            </div>
            <OptionalTextField requiredTriggers={['add;', 'delete;', 'push;']} fieldLabel={"Ref Filter"} fieldName={"branchRegex"}
                               description={"Trigger builds for matched branches or tags (example: \"release.*|hotfix.*|production\"). " +
                                            "Supported triggers: REF CREATED, PUSH EVENT, REF DELETED"}
                               id={id} jobInfo={jobInfo} errors={errors} updateText={updateText}/>
            <OptionalTextField requiredTriggers={['push;', 'propened;', 'prreopened;', 'prsourcerescoped;', 'prdestrescoped;', 'prmerged;', 'prdeclined;', 'prdeleted;']}
                               fieldLabel={"Monitored Paths"} fieldName={"pathRegex"}
                               description={"Trigger builds if matched files are modified (example: \"directory/.*.txt|foobar/.*\"). " +
                                            "Supported triggers: PUSH EVENT, PR OPENED, PR REOPENED, PR SOURCE RESCOPED, PR DEST RESCOPED, PR MERGED, PR DECLINED, PR DELETED"}
                               id={id} jobInfo={jobInfo} errors={errors} updateText={updateText}/>

            <div className={"field-group" + (jobInfo.active && jobInfo.triggers.includes('push;') ? "" : " hidden")}>
                <label htmlFor={"ignoreComitters-" + id}>Ignore Committers</label>
                <textarea id={"ignoreComitters-" + id} className={"textarea full-width-field"} name={"ignoreComitters-" + id}
                          value={jobInfo.ignoreComitters} rows={3} onChange={e => {updateText(id, 'ignoreComitters', e.target.value)}}/>
                <div className={"description"}>
                    {"List of bitbucket user names separated by new line, Commits from these users do not trigger the builds. " +
                     "(example: notifier) Supported triggers: PUSH EVENT"}
                </div>
            </div>

            <OptionalTextField requiredTriggers={['push;']}
                               fieldLabel={"Ignore Commits With String"} fieldName={"ignoreCommitMsg"}
                               description={"Regex string to ignore commits if it matches in a commit message. (example: \".*SkipCI.*\") " +
                               "Supported triggers: PUSH EVENT"}
                               id={id} jobInfo={jobInfo} errors={errors} updateText={updateText}/>

            <div className={"field-group" + (jobInfo.active && jobInfo.triggers.includes('manual;') ? "" : " hidden")}>
                <label htmlFor={"requirePermission-" + id}>Required Build Permission</label>
                <select id={"requirePermission-" + id} className={"select"} name={"requirePermission-" + id}
                       value={jobInfo.requirePermission} onChange={(e) => {updateText(id, "requirePermission", e.target.value)}}>
                    <option value={"REPO_READ"}>Read</option>
                    <option value={"REPO_WRITE"}>Write</option>
                    <option value={"REPO_ADMIN"}>Admin</option>
                </select>
                <div className={"description"}>
                    Only allow users with the given repository permission or higher to trigger this job.
                </div>
                {typeof errors["requirePermission-" + id] !== 'undefined' &&
                    <div className={"error"}>{errors["requirePermission-" + id]}</div>}
            </div>
            <OptionalTextField requiredTriggers={['propened;', 'prreopened;', 'prsourcerescoped;', 'prdestrescoped;', 'prmerged;', 'prdeclined;', 'prdeleted;']}
                               fieldLabel={"PR Destination Filter"} fieldName={"prDestinationRegex"}
                               description={"Trigger builds if the pull request destination matches the regex (example: \"release.*|hotfix.*|production\"). " +
                                            "Supported triggers: PR OPENED, PR REOPENED, PR SOURCE RESCOPED, PR DEST RESCOPED, PR MERGED, PR DECLINED, PR DELETED"}
                               id={id} jobInfo={jobInfo} errors={errors} updateText={updateText}/>
            <hr />
        </div>
    )
};

const mapStateToJobContainerProps = (state, ownProps) => {
    return {
        jobInfo: state.jobs[ownProps.id],
        errors: ownProps.errors,
        id: ownProps.id,
        jenkinsServers: state.jenkinsServers,
    }
};

const mapDispatchToJobContainerProps = (dispatch) => {
    return {
        toggleJob: (id) => {
            dispatch({
                type: 'TOGGLE_JOB',
                id: id
            });
        },
        deleteJob: (id) => {
            dispatch({
                type: 'DELETE_JOB',
                id: id
            });
        },
        updateText: (id, fieldName, fieldValue) => {
            dispatch({
                type: 'UPDATE_TEXT_FIELD',
                id: id,
                field: fieldName,
                value: fieldValue
            })
        },
        updateTrigger: (id, triggerVal) => {
            dispatch({
                type: 'UPDATE_TRIGGER_FIELD',
                id: id,
                value: triggerVal
            })
        }
    };
};

export const Job = connect(mapStateToJobContainerProps, mapDispatchToJobContainerProps)(JobContainer);

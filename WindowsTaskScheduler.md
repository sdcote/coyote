# Trouble Shooting

## Task Scheduler failed to start: 2147943785

Scheduled job fails with the below error message.

"Task Scheduler failed to start "\<task.name>" task for user "<domain\username>". Additional Data: Error Value: 2147943785."

### Solution
This problem may occur if the task account doesn’t have "logon as a batch job" privilege. 

* Click Start, type "gpedit.msc", 
* try to configure the following policy:
[Computer Configuration\Windows Settings\Security Settings\Local Policies\User Rights Assignment]
-Log on as a batch job.
Add the domain\username account and any others you may need.

* Retry the task.

This error might occur when the security policy settings prevent the user from logging on using a local user account.

* Open gpedit.msc, 
* Admin tools -> Local Security Policy -> User rights Assignnment -> Allow Local login. 
 
* Retry the task.

If you find you can't change the security options locally then there might be a group policy that is setting this at the domain or ou level.
How to set up your own server in less than 10 minutes
=====================================================

1. Put this script into you local Apache web server’s /cgi-bin/ folder:

**report-error.pl:**

#!/usr/bin/perl -w
use strict;
use CGI;

my $cgi = CGI->new;

my $data = $cgi->param('POSTDATA');
open my $FH, '>>', '/tmp/error-reports.log';
print $FH $data;
close $FH;

print $cgi->header;
print $cgi->start_html;
print $cgi->p('Thank you!');
print $cgi->end_html;

-EOF

2. Make sure the web server can execute ('chmod o+x report-error.pl')
3. Install the Eclipse error reporting plugin from our milestones site [1]
4. In your eclipse.ini or add the system property 
   '-Dorg.eclipse.recommenders.stacktraces.rcp.server-url=http://localhost/cgi-bin/report-error.pl'
5. Go create errors and see how your log file fills up.


Your log file (in '/tmp/error-reports.log') should contain lines like:

{"anonymousId":"f8087d7b-9d38-4c73-aeb1-6991603c2a84","eventId":"74056bb5-85e8-4c5b-aca5-5bb5072c7495","name":"","email":"","eclipseBuildId":"-","eclipseProduct":"org.eclipse.sdk.ide","javaRuntimeVersion":"1.8.0-b132","osgiWs":"cocoa","osgiOs":"MacOSX","osgiOsVersion":"10.9.4","osgiArch":"x86_64","presentBundles":[{"name":"org.eclipse.core.jobs","version":"3.6.0.v20140424-0053"},{"name":"org.eclipse.recommenders.stacktraces.rcp","version":"2.1.9.qualifier"}],"status":{"pluginId":"org.eclipse.recommenders.stacktraces.rcp","pluginVersion":"2.1.9.qualifier","code":0,"severity":4,"message":"status error message","fingerprint":"096ff7c0","exception":{"className":"java.lang.RuntimeException","message":"exception message","cause":{"className":"java.lang.IllegalArgumentException","message":"cause0","stackTrace":[{"fileName":"SampleAction.java","className":"org.eclipse.recommenders.stacktraces.rcp.actions.SampleAction$1","methodName":"run","lineNumber":35,"native_":false},{"fileName":"Worker.java","className":"org.eclipse.core.internal.jobs.Worker","methodName":"run","lineNumber":54,"native_":false}]},"stackTrace":[{"fileName":"HIDDEN","className":"HIDDEN","methodName":"HIDDEN","lineNumber":-1,"native_":false}]},"children":[]}}

To further process the data you, may use "jq" or any other tool to process the error log data [2].

HTH,
Marcel




[1] http://download.eclipse.org/recommenders/updates/milestones/
[2] http://stedolan.github.io/jq/
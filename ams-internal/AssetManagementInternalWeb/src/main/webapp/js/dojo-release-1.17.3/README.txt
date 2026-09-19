Dojo Toolkit 1.17.3
===================

The application vendors the Dojo Toolkit 1.17.3 release build into this directory and serves it as
static content: there is no npm, no package.json and no bundler anywhere in the build.

The toolkit is a third-party distribution of some eleven thousand files, so like the Oracle driver
and the CA certificate it is supplied per environment rather than committed. To restore it:

    curl -sL -o dojo.tgz https://download.dojotoolkit.org/release-1.17.3/dojo-release-1.17.3.tar.gz
    # expect: 9867938 bytes
    #         sha256 06a2719f69171a8b1dafca11a103c32dd192eff8a46fb5c768d1a47a09cbfaa0
    tar xzf dojo.tgz -C <this directory> --strip-components=1

That yields dojo/, dijit/, dojox/ and build-report.txt alongside this file, about 72 MB on disk.

Nothing in js/common.js depends on it. The pieces that matter - attaching the per-session AJAX
token to every state-changing request, and reloading when the server reports an expired session -
are written against the DOM directly, so the application works without the toolkit present.

Nothing currently loads it either: no JSP references dojo.js, and the asset grid and the calendar
pickers are plain markup fed by the JSON endpoints under /calendar and /ajax. The toolkit is here
so that those screens can be moved onto it; until one is, adding or removing this directory changes
nothing a user sees except the size of the WAR.

Note that WebSecurityConfig's content security policy allows 'unsafe-inline' and 'unsafe-eval'
specifically because this build requires both. Tightening that policy means replacing the toolkit
first.

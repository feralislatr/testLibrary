// ==================== pipeline =======================

stage('Initialize') {
      node() {

        //Get current commit from github
        checkout scm

        //check if dockerfile exists before building
        if (!fileExists('Dockerfile')) {
          Globals.failureMessage = "No Dockerfile"
          throw new IOException("No Dockerfile")
        }

        // Get the git commit hash by running a shell command and returning stdout
        git_sha = sh (
          script: 'git rev-parse HEAD',
          returnStdout: true
        ).trim().take(6)

        // get the remote url to get org name and repo name
        def git_url = sh (
          script: 'git ls-remote --get-url',
          returnStdout: true
        ).trim() - '.git'
        echo "git url: $git_url"

        // get the org name and repo name from the url
        def tokens = git_url.tokenize('/')
        org_name = tokens[tokens.size()-2]
        repo_name = tokens[tokens.size()-1]

        stash includes: '*', name: env.BUILD_TAG
      }
    }

    node() {
        unstash env.BUILD_TAG

        utils.syntax_check(ghcert, common)
        utils.build(ghcert,repo_name, git_sha, dockerhub, dockeruser, dockercert)
        utils.unit_tests(ghcert, repo_name, git_sha, dockeruser, common)
      }



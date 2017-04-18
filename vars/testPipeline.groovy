// ==================== pipeline =======================
def start() {

  def img

  stage('Initialize') {
        node() {

          //Get current commit from github
          checkout scm

          utils.initialize()

          // //check if dockerfile exists before building
          // if (!fileExists('Dockerfile')) {
          //   Globals.failureMessage = "No Dockerfile"
          //   throw new IOException("No Dockerfile")
          // }

          // // Get the git commit hash by running a shell command and returning stdout
          // git_sha = sh (
          //   script: 'git rev-parse HEAD',
          //   returnStdout: true
          // ).trim().take(6)

          // // get the remote url to get org name and repo name
          // def git_url = sh (
          //   script: 'git ls-remote --get-url',
          //   returnStdout: true
          // ).trim() - '.git'
          // echo "git url: $git_url"

          // // get the org name and repo name from the url
          // def tokens = git_url.tokenize('/')
          // repo_name = tokens[tokens.size()-1]
          // echo "repo: $repo_name"

          stash includes: '*', name: env.BUILD_TAG
        }
      }

        node() {
          // Get all the files
          unstash BUILD_TAG
          // do syntax check
          stage('Syntax Check') {
            utils.syntax_check()
          }

          //build image
          stage('Build Image') {
            img = utils.build()
          }
          // do unit tests
          stage('Unit Tests') {
            utils.unit_tests(img)
          }

        }

}

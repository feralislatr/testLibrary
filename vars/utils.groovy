// vars/utils.groovy



// sets some useful environment variables and make sure all necessary files are present
def initialize() {

  try {
    // Put github related stuff in variables
    def tokens = "$JOB_NAME".tokenize('/')

    env.REPO_NAME = tokens[1].toLowerCase()

    // Get the git commit hash by running a shell command and returning stdout
    env.GIT_SHA = sh( script: 'git rev-parse HEAD', returnStdout: true ).trim()

    }


    // if this is a nodejs project
    if (fileExists('package.json')) {
      env.PROJ_TYPE = "Node"
      // Syntax check command for nodejs
      env.SYNTAX_COMMAND = "docker run -u \$(id -u) --name $BUILD_TAG-syntax -v \$(pwd):/app -w /app node:argon /bin/bash -c \'walk() { cd \"\$1\"; for file in *; do if [ -d \"\$file\" ] && [ \"\$file\" != \"node_modules\" ]; then walk \"\$1/\$file\"; else if [[ ${file: -3} == \".js\" ]]; then node -c \"\$file\"; fi; fi; cd \"\$1\"; done; }; walk `pwd`\'"
      // Unit test command for nodejs
      env.UNIT_COMMAND   = "npm test"
    // if this is a java project
    } else if (fileExists('pom.xml')) {
      env.PROJ_TYPE = "Java"
      // Syntax command for java
      env.SYNTAX_COMMAND = "docker run --name $BUILD_TAG-syntax -v \$(pwd):/app -w /app maven:3.3.9-jdk-8 mvn package clean -DskipTests"
      // Syntax command for java
      env.UNIT_COMMAND   = "mvn test -Dmaven.test.failure.ignore=false"
    } else {
      throw new IOException("Missing package.json or pom.xml")
    }

    // Make sure dockerfile exists
    if (!fileExists('Dockerfile')) {
      throw new IOException("Dockerfile not found")
    }


  } catch (err) {
    throw err
  }

}

// Run Syntax Check
def syntax_check(){


  def exit_code = sh (
    script: SYNTAX_COMMAND,
    returnStatus: true
  )

  // record logs in workspace
  sh "docker logs $BUILD_TAG-syntax 2>&1 > log.txt"

  //remove syntax-test container
  sh "docker rm $BUILD_TAG-syntax"

  if (exit_code != 0) {
    echo "Syntax Check Returned Errors"
    github.setCommitStatus("Syntax Check", "Syntax Check Returned Errors", "FAILURE")
    throw new Exception("Syntax Check Returned Errors")
  } else{
    echo "Syntax Check Completed Successfully"
  }

}

//Build Image and return the object
def build() {
  def testImg

  try {
    //withCredentials([[$class: 'UsernamePasswordMultiBinding', credentialsId: config.dockerhubCert, usernameVariable: 'USERNAME', passwordVariable: 'PASSWORD']]) {
      //Define image for running CI tests
      //testImg = docker.build("$config.dockerhubUrl/$USERNAME/$REPO_NAME:$GIT_SHA")
      testImg = docker.build("$REPO_NAME:$GIT_SHA")
      sh "docker images | grep $GIT_SHA"
   // }
  } catch (err) {
    echo "Image failed to build"
    throw new Exception("Image failed to build")
  }

  return testImg
}

// Run unit tests
def unit_tests(Object img){

  // run unit tests for image
  exit_code = sh (
    script: "docker run --name $BUILD_TAG-unit $img.id $UNIT_COMMAND",
    returnStatus: true
  )

  // record logs in workspace
  sh "docker logs $BUILD_TAG-unit 2>&1 > log.txt"

  // remove unit-test container
  sh "docker rm $BUILD_TAG-unit"

  // fail if tests failure detected
  if (exit_code != 0) {
    echo "Unit Test Failure"
    throw new Exception("Image has failed unit test(s)")
  } else{
    echo "Unit Tests Completed Successfully"
  }

  if(env.PROJ_TYPE == "Node"){
    archiveArtifacts artifacts: '**/log.txt', fingerprint: true
  }else{
    //Java
    step([$class: 'JUnitResultArchiver', testResults: '**/target/surefire-reports/TEST-*.xml'])
  }

}

// Remove all images with a certain ID
void removeImages(String image_id) {
  // Don't output an error so people don't get confused
  sh "docker rmi -f $image_id 2> /dev/null || echo 'image already deleted'"
}


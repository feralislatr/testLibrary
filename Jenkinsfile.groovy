#!groovy
// Jenkinsfile

// Load the global library
@Library('testLibrary') _
import hudson.Util

// def org_name = "GSA-IAE-TEST"
def org_name = ""

def numTests = 0
def numPassed = 0

// stage("TODO: Successful Deployment Through Prodlike") {
// 	// TODO: Figure out how to automate PRs with a Jenkinsfile
//
// 	// if (testPassed(org_name, "CICD-food-##", "feature-branch", "SUCCESS")) {
// 	// 	numPassed += 1
// 	// }
// 	// create feature-to-develop pr
// 	// wait for feature-to-develop pr job to finish
// 	// merge feature-to-develop pr
// 	// wait for develop job to finish
// 	// create develop-to-master pr
// 	// wait for develop-to-master pr job to finish
// 	// merge develop-to-master pr
// 	// wait for master job to finish
//
// 	numTests += 1
// }
stage("Java Service CI") {
	def tests = ["test-01", "test-02", "test-03", "test-04", "test-05"]

	// The map we'll store the parallel steps in before executing them.
	def stepsForParallel = [:]

	// The standard 'for (String s: stringsToEcho)' syntax also doesn't work, so we
	// need to use old school 'for (int i = 0...)' style for loops.
	for (int i = 0; i < tests.size(); i++) {

		def branch_name = tests.get(i)

		stepsForParallel[branch_name] = {
			if (testPassed(org_name, "CICD-CI-Java", branch_name, "FAILURE")) {
				numPassed += 1
			}
			numTests += 1
		}
	}
	parallel stepsForParallel
}
stage("nodeJS Service CI") {

	def tests = ["test-06", "test-07", "test-01", "test-08", "test-09"]

	// The map we'll store the parallel steps in before executing them.
	def stepsForParallel = [:]

	// The standard 'for (String s: stringsToEcho)' syntax also doesn't work, so we
	// need to use old school 'for (int i = 0...)' style for loops.
	for (int i = 0; i < tests.size(); i++) {

		def branch_name = tests.get(i)

		stepsForParallel[branch_name] = {
			if (testPassed(org_name, "CICD-CI-Node", branch_name, "FAILURE")) {
				numPassed += 1
			}
			numTests += 1
		}
	}
	parallel stepsForParallel
}
stage("Application Containers Comp Deploy Failure") {

	if (testPassed(org_name, "CICD-food-13", "develop", "FAILURE")) {
		numPassed += 1
	}
	numTests += 1
}
// stage("Application Not Approved for MinC Deploy") {
//
// 	// must manually abort MinC for now
// 	if (testPassed(org_name, "CICD-food-16", "master", "ABORTED")) {
// 		numPassed += 1
// 	}
// 	numTests += 1
// }
stage("Application Containers MinC Deploy Failure") {

	// must manually proceed MinC for now
	if (testPassed(org_name, "CICD-food-18", "master", "FAILURE")) {
		numPassed += 1
	}
	numTests += 1
}
// stage("Application Not Approved for Prodlike Deploy") {
//
// 	// must manually proceed MinC and abort Prodlike for now
// 	if (testPassed(org_name, "CICD-food-18", "master", "ABORTED")) {
// 		numPassed += 1
// 	}
// 	numTests += 1
// }
// stage("TODO: Apply CI Build Workflow Against General PR") {
// 	// TODO: Create repo and pr for this test case
//
// 	// if (testPassed(org_name, "CICD-food-##", "PR-##", "SUCCESS")) {
// 	// 	numPassed += 1
// 	// }
// 	numTests += 1
// }
// stage("TODO: Reference to Unresolvable Submodule") {
// 	// TODO: Create repo and branch for this test case
//
// 	// if (testPassed(org_name, "CICD-food-##", "feature-branch", "FAILURE")) {
// 	// 	numPassed += 1
// 	// }
// 	numTests += 1
// }
// stage("TODO: Successful Deployment to Comp - nodeJS Submodule") {
// 	numTests += 1
// }
// stage("TODO: Successful Deployment to Comp - Java Submodule") {
// 	numTests += 1
// }
// stage("TODO: Special Case: Successful Deployment to MinC from a Sub-branch") {
// 	numTests += 1
// }
// stage("TODO: Special Case: Successful Deployment to Prodlike from a Sub-branch") {
// 	numTests += 1
// }
// stage("TODO: Prohibit PR from Sub-branch into the Master Branch") {
// 	numTests += 1
// }
// stage("TODO: Special Case: Allowed PR from Sub-branch into the Master Branch") {
// 	numTests += 1
// }

percent = (numPassed / numTests) * 100

echo """\
Tests run:          $numTests
Tests passed:       $numPassed
Success percentage: $percent%
"""

def testPassed(String org_name, String repo_name, String branch_name, String desiredResult) {
	def test_case
	try {
		def result
		test_case = build job: "/$org_name/$repo_name/$branch_name", propagate: false
		if (test_case.result == desiredResult) {
			echo """\
$org_name » $repo_name » $branch_name
result:   $test_case.result ✓"""
			return true
		} else {
			echo """\
$org_name » $repo_name » $branch_name
result:   $test_case.result ✗"""
		}
	} catch (err) {
		print err
		echo "$org_name » $repo_name » $branch_name could not be completed ✗"
	}
	currentBuild.result = "UNSTABLE"
	return false
}

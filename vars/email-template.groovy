<STYLE>
  BODY, TABLE, TD, TH, P {
    font-family: Calibri, Verdana, Helvetica, sans serif;
    font-size: 12px;
    color: black;
  }
  .console {
    font-family: Courier New;
  }
  .filesChanged {
    width: 10%;
    padding-left: 10px;
  }
  .section {
    width: 100%;
    border: thin black dotted;
  }
  .td-title-main {
    color: white;
    font-size: 200%;
    padding-left: 5px;
    font-weight: bold;
  }
  .td-title {
    color: white;
    font-size: 120%;
    font-weight: bold;
    padding-left: 5px;
    text-transform: uppercase;
  }
  .td-title-tests {
    font-weight: bold;
    font-size: 120%;
  }
  .td-header-maven-module {
    font-weight: bold;
    font-size: 120%;    
  }
  .td-maven-artifact {
    padding-left: 5px;
  }
  .tr-title {
    background-color: <%= (build.result == null || build.result.toString() == 'SUCCESS') ? '#27AE60' : build.result.toString() == 'FAILURE' ? '#E74C3C' : '#f4e242' %>;
  }
  .test {
    padding-left: 20px;
  }
  .test-fixed {
    color: #27AE60;
  }
  .test-failed {
    color: #E74C3C;
  }
</STYLE>
<BODY>
  <!-- BUILD RESULT -->
  <table class="section">
    <tr class="tr-title">
      <td class="td-title-main" colspan=2>
        BUILD ${build.result ?: 'COMPLETED'}
      </td>
    </tr>
    <tr>
      <td>URL:</td>
      <td><A href="${rooturl}${build.url}">${rooturl}${build.url}</A></td>
    </tr>
    <tr>
      <td>Project:</td>
      <td>${project.name}</td>
    </tr>
    <tr>
      <td>Date:</td>
      <td>${it.timestampString}</td>
    </tr>
    <tr>
      <td>Duration:</td>
      <td>${build.durationString}</td>
    </tr>
    <tr>
      <td>Cause:</td>
      <td><% build.causes.each() { cause -> %> ${cause.shortDescription} <%  } %></td>
    </tr>
	<tr>
      <td>Description:</td>
      <td><strong> ${build.description} </strong></td>
    </tr>
  </table>
  <br/>

  <!-- CHANGE SET -->
  <%
  def changeSets = build.changeSets
  if(changeSets != null) {
    def hadChanges = false %>
  <table class="section">
    <tr class="tr-title">
      <td class="td-title" colspan="2">CHANGES</td>
    </tr>
    <% changeSets.each() { 
      cs_list -> cs_list.each() { 
        cs -> hadChanges = true %>
    <tr>
      <td>
        Revision
        <%= cs.metaClass.hasProperty('commitId') ? cs.commitId : cs.metaClass.hasProperty('revision') ? cs.revision : cs.metaClass.hasProperty('changeNumber') ? cs.changeNumber : "" %>
        by <B><%= cs.author %></B>
      </td>
      <td>${cs.msgAnnotated}</td>
    </tr>
        <% cs.affectedFiles.each() {
          p -> %>
    <tr>
      <td class="filesChanged">${p.editType.name}</td>
      <td>${p.path}</td>
    </tr>
        <% }
      }
    }
    if ( !hadChanges ) { %>
    <tr>
      <td colspan="2">No Changes</td>
    </tr>
    <% } %>
  </table>
  <br/>
  <% } %>

<!-- ARTIFACTS -->
  <% 
  def artifacts = build.artifacts
  if ( artifacts != null && artifacts.size() > 0 ) { %>
  <table class="section">
    <tr class="tr-title">
      <td class="td-title">BUILD ARTIFACTS</td>
    </tr>
    <% artifacts.each() {
      f -> %>
      <tr>
        <td>
          <a href="${rooturl}${build.url}artifact/${f}">${f}</a>
      </td>
    </tr>
    <% } %>
  </table>
  <br/>
  <% } %>

<!-- MAVEN ARTIFACTS -->
  <%
  try {
    def mbuilds = build.moduleBuilds
    if ( mbuilds != null ) { %>
  <table class="section">
    <tr class="tr-title">
      <td class="td-title">BUILD ARTIFACTS</td>
    </tr>
      <%
      try {
        mbuilds.each() {
          m -> %>
    <tr>
      <td class="td-header-maven-module">${m.key.displayName}</td>
    </tr>
          <%
          m.value.each() { 
            mvnbld -> def artifactz = mvnbld.artifacts
            if ( artifactz != null && artifactz.size() > 0) { %>
    <tr>
      <td class="td-maven-artifact">
              <% artifactz.each() {
                f -> %>
        <a href="${rooturl}${mvnbld.url}artifact/${f}">${f}</a><br/>
              <% } %>
      </td>
    </tr>
            <% }
          }
        }
      } catch(e) {
        // we don't do anything
      } %>
  </table>
  <br/>
    <% }
  } catch(e) {
    // we don't do anything
  } %>

<!-- JUnit TEMPLATE -->

  <%
  def junitResultList = it.JUnitTestResult
  try {
    def cucumberTestResultAction = it.getAction("org.jenkinsci.plugins.cucumber.jsontestsupport.CucumberTestResultAction")
    junitResultList.add( cucumberTestResultAction.getResult() )
  } catch(e) {
    //cucumberTestResultAction not exist in this build
  } %>
  
  <table class="section">
    <tr class="tr-title">
	
	<% if ( junitResultList.size() > 0 ) { %>
      <td class="td-title" colspan="5">${junitResultList.first().displayName}</td>
	<% } else { %>  
	  <td class="td-title" colspan="5"> JUnit Test</td>		
	<% } %>
    </tr>
    <tr>
        <td class="td-title-tests">Name</td>
        <td class="td-title-tests">Failed</td>
        <td class="td-title-tests">Passed</td>
        <td class="td-title-tests">Skipped</td>
        <td class="td-title-tests">Total</td>
      </tr>
    <% junitResultList.each {
      junitResult -> junitResult.getChildren().each {
        packageResult -> %>
    <tr>
      <td>${packageResult.getName()}</td>
      <td>${packageResult.getFailCount()}</td>
      <td>${packageResult.getPassCount()}</td>
      <td>${packageResult.getSkipCount()}</td>
      <td>${packageResult.getPassCount() + packageResult.getFailCount() + packageResult.getSkipCount()}</td>
    </tr>
    <% packageResult.getPassedTests().findAll({it.getStatus().toString() == "FIXED";}).each{
        test -> %>
            <tr>
              <td class="test test-fixed" colspan="5">
                ${test.getFullName()} ${test.getStatus()}
              </td>
            </tr>
        <% } %>
        <% packageResult.getFailedTests().sort({a,b -> a.getAge() <=> b.getAge()}).each{
          failed_test -> %>
    <tr>
      <td class="test test-failed" colspan="5">
        ${failed_test.getFullName()} (Age: ${failed_test.getAge()})
      </td>
    </tr>
        <% }
      }
    } %>
  </table>
  <br/>
  
<!-- Robot -->

<table class="section">
    <tr class="tr-title">
      <td class="td-title">Robot Test Result</td>
    </tr>
	
	<tr>
		<td>
		
<%
 import java.text.DateFormat
 import java.text.SimpleDateFormat
 %>
 <!-- Robot Framework Results -->
 <%
 def robotResults = false
 def actions = build.actions // List<hudson.model.Action>
 actions.each() { action ->
    if( action != null && action.class != null && action.class.simpleName.equals("RobotBuildAction") ) { // hudson.plugins.robot.RobotBuildAction
        robotResults = true %>

      <table cellspacing="0" cellpadding="4" border="1" align="left">
        <thead>
          <tr bgcolor="#F3F3F3">
            <td><b>Type</b></td>
            <td><b>Total</b></td>
            <td><b>Passed</b></td>
            <td><b>Failed</b></td>
            <td><b>Pass %</b></td>
          </tr>
        </thead>

        <tbody>

          <tr><td><b>All Tests</b></td>
            <td><%= action.result.overallTotal %></td>
            <td><%= action.result.overallPassed %></td>
            <td><%= action.result.overallFailed %></td>
            <td><%= action.overallPassPercentage %></td>
          </tr>

          <tr><td><b>Critical Tests</b></td>
            <td><%= action.result.criticalTotal %></td>
            <td><%= action.result.criticalPassed %></td>
            <td><%= action.result.criticalFailed %></td>
            <td><%= action.criticalPassPercentage %></td>
          </tr>

        </tbody>
      </table><%
    } // robot results
 }
 if (!robotResults) { %>
 <p>No Robot Framework test results found.</p>
 <%
 } %>
		
		</td>
	</tr>
	
</table>

  

<!-- CONSOLE OUTPUT -->
  <%
  if ( build.result == hudson.model.Result.FAILURE ) { %>
  <table class="section" cellpadding="0" cellspacing="0">
    <tr class="tr-title">
      <td class="td-title">CONSOLE OUTPUT</td>
    </tr>
    <% 	build.getLog(100).each() {
      line -> %>
	  <tr>
      <td class="console">${org.apache.commons.lang.StringEscapeUtils.escapeHtml(line)}</td>
    </tr>
    <% } %>
  </table>
  <br/>
  <% } %>
</BODY>
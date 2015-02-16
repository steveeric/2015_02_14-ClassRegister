package jp.pmw.class_register;

public class TMP_CLASS_SCHEDULE_MST {

	public String tmpRandomNo;
	public String universityName;
	public String deptName;
	public String majorName;
	public String enrollmentPeriod;
	public int enrollmentPeriodJudge;
	public String grade;
	public String year;
	public String semester;
	public String dayName;
	public String timeSection;
	public String timetableName;
	public String classStartTime;
	public String classEndTime;
	public String subjectNumber;
	public String subjectRemarks;
	public String subjectName;
	public String facultyIdNumber;
	public String notOverlapName;
	public String campusName;
	public String roomName;
	public int comleteFlag;

	public void setTMP_RANDOM_NO(String tmpRandomNo){
		this.tmpRandomNo = tmpRandomNo;
	}

	public void setUNIVERSITY_NAME(String universityName){
		 this.universityName = universityName;
	}
	public void setDEPT_NAME(String deptName){
		 this.deptName = deptName;
	}
	public void setMAJOR_NAME(String majorName){
		this.majorName = majorName;
	}
	public void setENROLLMENT_PERIOD(String enrollmentPeriod){
		this.enrollmentPeriod = enrollmentPeriod;
	}
	public void setENROLLMENT_PERIOD_JUDGE(int enrollmentPeriodJudge){
		this.enrollmentPeriodJudge =enrollmentPeriodJudge;
	}

	public void setGRADE(String grade){
		 this.grade = grade;
	}
	public void setYEAR(String year){
		 this.year = year;;
	}
	public void setSEMESTER(String semester){
		 this.semester = semester;
	}
	public void setDAY_NAME(String dayName){
		 this.dayName = dayName;
	}
	public void setTIME_SECTION(String timeSection){
		 this.timeSection = timeSection;
	}
	public void setTIMETABLE_NAME(String timetableName){
		 this.timetableName = timetableName;
	}
	public void setCLASS_START_TIME(String classStartTime){
		 this.classStartTime = classStartTime;
	}
	public void setCLASS_END_TIME(String classEndTime){
		 this.classEndTime = classEndTime;
	}
	public void setSUBJECT_NUMBER(String subjectNumber){
		 this.subjectNumber = subjectNumber;
	}
	public void setSUBJECT_NAME(String subjectName){
		 this.subjectName = subjectName;
	}
	public void setSUBJECT_REMARKS(String subjectRemarks){
		 this.subjectRemarks = subjectRemarks;
	}
	public void setFACULTY_ID_NUMBER(String facultyIdNumber){
		 this.facultyIdNumber = facultyIdNumber;
	}
	public void setNot_OVERLAP_NAME(String notOverlapName){
		 this.notOverlapName = notOverlapName;
	}
	public void setCAMPUS_NAME(String campusName){
		 this.campusName = campusName;
	}
	public void setROOM_NAME(String roomName){
		 this.roomName = roomName;
	}
	public void setCOMPLETE_FLAG(int cmpFlag){
		this.comleteFlag = cmpFlag;
	}




	public String getTMP_RANDOM_NO(){
		return this.tmpRandomNo;
	}

	public String getUNIVERSITY_NAME(){
		 return this.universityName;
	}
	public String getDEPT_NAME(){
		 return this.deptName;
	}
	public String getMAJOR_NAME(){
		return this.majorName;
	}
	public String getENROLLMENT_PERIOD(){
		return this.enrollmentPeriod;
	}
	public int getENROLLMENT_PERIOD_JUDGE(){
		return this.enrollmentPeriodJudge;
	}
	public String getGRADE(){
		 return this.grade;
	}
	public String getYEAR(){
		 return this.year;
	}
	public String getSEMESTER(){
		 return this.semester;
	}
	public String getDAY_NAME(){
		 return this.dayName;
	}
	public String getTIME_SECTION(){
		 return this.timeSection;
	}
	public String getTIMETABLE_NAME(){
		 return this.timetableName;
	}
	public String getCLASS_START_TIME(){
		 return this.classStartTime;
	}
	public String getCLASS_END_TIME(){
		 return this.classEndTime;
	}
	public String getSUBJECT_NUMBER(){
		 return this.subjectNumber;
	}
	public String getSUBJECT_NAME(){
		 return this.subjectName;
	}
	public String getSUBJECT_REMARKS(){
		 return this.subjectRemarks;
	}
	public String getFACULTY_ID_NUMBER(){
		 return this.facultyIdNumber;
	}
	public String getNot_OVERLAP_NAME(){
		 return this.notOverlapName;
	}
	public String getCAMPUS_NAME(){
		 return this.campusName;
	}
	public String getROOM_NAME(){
		 return this.roomName;
	}
	public int getCOMPLETE_FLAG(){
		return this.comleteFlag;
	}
}


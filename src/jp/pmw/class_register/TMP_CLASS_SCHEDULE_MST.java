package jp.pmw.class_register;

public class TMP_CLASS_SCHEDULE_MST {

	public String tmpRandomNo;
	public String universityName;
	public String deptName;
	public String majorName;
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
		 this.roomName = campusName;
	}
	public void setCOMPLETE_FLAG(int cmpFlag){
		this.comleteFlag = cmpFlag;
	}
}


package jp.pmw.class_register;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.jenkov.db.PersistenceManager;
import com.jenkov.db.itf.IDaos;
import com.jenkov.db.itf.PersistenceException;
import jp.pmw.id_generator.IdGenerator;
import jp.pmw.log.MyLog;
import jp.pmw.my.time.MyTime;
import jp.pmw.mysql.Connect;
import jp.pmw.sitandgo.config.MyConfig;
import jp.pmw.util.error.UtilError;

public class ClassRegister {
	//TMPテーブル名
	private String tmpMstTableName = MyConfig.DB_TABLE_TMP_CLASS_SCHEDULE_MST;
	//TMP授業時間割リスト
	List<TMP_CLASS_SCHEDULE_MST> tmpList;

	public ClassRegister(){
		try {
			//TMPデータ取得
			getTmpClassScheduleMst();
			if(tmpList.size() == 0){
				//処理するアイテムがない.
				MyLog.getInstance().info("移行する授業データが存在しません.");
			}else{
				//取得したTMPデータをふくらませ
				//CLASSES_MSTテーブルに格納していく.
				oneItemProcess();
			}
		} catch (PersistenceException e) {
			MyLog.getInstance().error(e.getMessage());
		} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			MyLog.getInstance().error(e.getMessage());
			UtilError.showError(e.getCause());
		}
	}

	/**
	 * createdate : 2015年2月14日
	 * getTmpClassScheduleMstメソッド
	 * DBのTMP_CLSSS_SCHEDULE_MSTに登録されているデータを取得
	 * @throws PersistenceException
	 **/
	private void getTmpClassScheduleMst() throws PersistenceException{
		TmpData<TMP_CLASS_SCHEDULE_MST> tmpdata = new TmpData<TMP_CLASS_SCHEDULE_MST>();
		this.tmpList = tmpdata.getTmpMst(TMP_CLASS_SCHEDULE_MST.class, tmpMstTableName);
	}

	/**
	 * createdate : 2015年2月14日
	 * oneItemProcessメソッド
	 * リストに格納されているデータを一軒ずつ処理る
	 * @throws SQLException
	 * @throws PersistenceException
	 **/
	private void oneItemProcess() throws SQLException, PersistenceException{
		PersistenceManager manager = new PersistenceManager();
		IDaos daos = manager.createDaos(Connect.getInstance().getConnection());;
		for(int i=0; i<this.tmpList.size(); i++){
			//1件の授業を膨らます.
			boolean b = registOneData(tmpList.get(i));
			if(b==false){
				MyLog.getInstance().error("TMP乱数:"+tmpList.get(i).tmpRandomNo);
			}else{
				tmpList.get(i).setCOMPLETE_FLAG(1);
				//更新
				int reuslt = daos.getObjectDao().update(tmpList.get(i));
				if(reuslt != 1){
					MyLog.getInstance().error("TMP_CLASS_SCHEDULE_MSTの使用済み処理が行えませんでした.TMP乱数:"+tmpList.get(i).tmpRandomNo);
				}
			}
		}
	}
	/**
	 * createdate : 2015年2月14日
	 * registOneDataメソッド
	 * 一件づつCLAA_MSTにデータを登録する.
	 * @param TMP_CLASS_SCHEDULE_MST tmpテーブルに格納されているデータ
	 * @throws SQLException
	 **/
	private boolean registOneData(TMP_CLASS_SCHEDULE_MST tmp) throws SQLException{
		String universityname = tmp.universityName;
		String deptName = tmp.deptName;
		String deptId = getDeptId(universityname,deptName);
		if(deptId == null){
			return false;
		}

		String year = tmp.year;
		String timeSection = tmp.timeSection;
		String timetableName = tmp.timetableName;
		String classStartTime = tmp.classStartTime;
		String classEndTime = tmp.classEndTime;
		String subjectNumber = tmp.subjectNumber;
		String subjectName = tmp.subjectName;
		String subjectRemarks = tmp.subjectRemarks;
		String semester = tmp.semester;
		String dayName = tmp.dayName;

		//教員情報
		String facultyIdNumber = tmp.facultyIdNumber;
		String notOverlapName = tmp.notOverlapName;

		//場所情報
		String campusName = tmp.campusName;
		String roomName = tmp.roomName;


		/*DBコミットを忘れずに!*/
		Connect.getInstance().getConnection().setAutoCommit(false);


		String timetableId = null;
		String subjectId = null;
		//時限調査
		timetableId = sarchSameRegistTimetable(deptId,year,timeSection,timetableName,classStartTime,classEndTime);
		if(timetableId == null){
			//登録されていない.
			timetableId = registTimetableMst(deptId,year,timeSection,timetableName,classStartTime,classEndTime);
		}
		if(timetableId == null){
			//処理に失敗したのでr-るバック
			Connect.getInstance().getConnection().rollback();
			return false;
		}

		//授業情報調査
		subjectId = serchSameRegistSubject(deptId,year,subjectNumber,subjectName,subjectRemarks);
		if(subjectId == null){
			subjectId = registSubjectMst(deptId,year,subjectNumber,subjectName,subjectRemarks);
		}
		if(subjectId == null){
			//処理に失敗したのでr-るバック
			Connect.getInstance().getConnection().rollback();
			return false;
		}

		//timetableId
		//subjectId
		//正常!

		//年度と学期と曜日
		//アカデミックカレンダーのID群を取得する.
		List<String> acList = getAcademicCalendarIds(deptId,year,semester,dayName);
		if(acList.size() == 0){
			//処理に失敗したのでr-るバック
			MyLog.getInstance().info("学年歴を取得できませんでした. "+"学科ID:"+deptId+",西暦:"+year+",学期名:"+semester+",曜日名:"+dayName);
			Connect.getInstance().getConnection().rollback();
			return false;
		}

		MyLog.getInstance().info("学科ID:"+deptId+",西暦:"+year+",学期名:"+semester+",曜日名:"+dayName+"から、「"+acList.size()+"」件の学年歴を取得できました.");


		//教員情報取得
		String facultyId = null;
		//if(facultyIdNumber != null || (!(facultyIdNumber.equals(""))) ){
		if( (!(facultyIdNumber.equals(""))) ){
		//大学独自の教員ID番号入力値があった場合
			facultyId = convertFacultyIdFromFacultyIdNumber(deptId,facultyIdNumber);
		}else if(notOverlapName != null){
			facultyId = convertFacultyIdFromNotOverlapName(notOverlapName);
		}

		if(facultyId == null || facultyId.equals("")){
			//処理に失敗したのでr-るバック
			MyLog.getInstance().error("教員ID番号がありません.");
			Connect.getInstance().getConnection().rollback();
			return false;
		}

		//教室情報を取得する.
		String roomId = getRoomId(campusName,roomName);
		if(roomId == null){
			Connect.getInstance().getConnection().rollback();
			return false;
		}

		//授業データを一括でインサーチするSQL文を作成する.
		//インサートする.
		String classSQL = createInsertClassesMstSQL(acList,timetableId,roomId,facultyId,subjectId);
		int result = insertClassData(classSQL);
		if(result == 0){
			MyLog.getInstance().error("授業データがDBに登録できませんでした.SQL:"+classSQL);
			Connect.getInstance().getConnection().rollback();
			return false;
		}else if(acList.size() != result){
			MyLog.getInstance().error("学年暦のサイズ「"+acList.size()+"」と登録授業日のサイズ「"+result+"」が異なります.SQL:"+classSQL);
			Connect.getInstance().getConnection().rollback();
			return false;
		}

		//授業データ正常登録処理をおこないました.
		MyLog.getInstance().info("「"+acList.size()+"」件の授業データをDBに登録しました."+classSQL);

		/*何もなければコミットしてね*/
		Connect.getInstance().getConnection().commit();
		return true;
	}
	/**
	 * createdate : 2015年2月14日
	 * insertClassDataメソッド
	 * @param sql 授業データ一括インサートSQL
	 * @return result インサート結果
	 * @throws SQLException
	 **/
	private int insertClassData(String sql) throws SQLException{
		int result = 0;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = Connect.getInstance().getConnection().prepareStatement(sql);
			result = ps.executeUpdate();
		} finally {
			if(ps != null){
				ps.close();
			}
			if(rs != null){
				rs.close();
			}
		}
		return result;
	}

	/**
	 * createdate : 2015年2月14日
	 * createInsertClassesMstSQLメソッド
	 * 授業IDを一括でインサートするSQL文を作成
	 * @param academicCalendars 学年暦ID群
	 * @param timetableId 時限ID
	 * @param roomId 教室名
	 * @param facultyId 教員ID(複数の場合は「＆」で区切られる
	 * @param subjectId 科目ID
	 * @throws SQLException
	 * @return String 授業データ一括インサートSQL
	 **/
	private String createInsertClassesMstSQL(List<String> academicCalendars,String timetableId,String roomId,String facultyId,String subjectId){
		String startSQL = "INSERT INTO `CLASSES_MST` (`CANCELED`, `CLASS_TOTAL`, `ACADEMIC_CALENDAR_ID`, `TIMETABLE_ID`, `SUBJECT_ID`, `FACULTY_ID`, `ROOM_ID`, `RECORED_INSERT_DATE_TIME`, `LAST_UPDATE_TIME`) VALUES ";
		String sql = startSQL;
		int classTotal = 0;
		for(int i=0; i<academicCalendars.size(); i++){
			++classTotal;
			String acId = academicCalendars.get(i);
			String midSQL = "('0', "+classTotal+", '"+acId+"', '"+timetableId+"', '"+subjectId+"', '"+facultyId+"', '"+roomId+"', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

			sql = sql + midSQL;
			if(academicCalendars.size() -1 != i){
				sql = sql + ",";
			}
		}
		return sql;
	}

	/**
	 * createdate : 2015年2月14日
	 * getRoomIdメソッド
	 * 教室IDを取得する.(カンパス名は,NULLでも対応可)
	 * @param campusName キャンパス名
	 * @param roomName 全教室名
	 * @throws SQLException
	 * @return List<String> 学年歴ID
	 **/
	private String getRoomId(String campusName,String roomName) throws SQLException{
		String roomId = null;
		String sql = null;
		if(campusName == null){
			sql = "SELECT ROOM_ID FROM `ROOMS_MST` WHERE `ROOM_NAME` LIKE '"+roomName+"'";
		}else{
			if(campusName.equals("")){
				sql = "SELECT ROOM_ID FROM `ROOMS_MST` WHERE `ROOM_NAME` LIKE '"+roomName+"'";
			}else{
				sql = "SELECT ROOM_ID FROM `ROOMS_MST` WHERE `CAMPUS_NAME` LIKE '"+campusName+"' AND `ROOM_NAME` LIKE '"+roomName+"'";
			}
		}
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = Connect.getInstance().getConnection().prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs.next()){
				roomId = rs.getString("ROOM_ID");
			}
		} finally {
			if(ps != null){
				ps.close();
			}
			if(rs != null){
				rs.close();
			}
		}
		//教室IDが取得できなかった場合.
		if(roomId == null){
			MyLog.getInstance().error("教室IDを取得できませんでした.SQL:"+sql);
		}

		return roomId;
	}

	/**
	 * createdate : 2015年2月14日
	 * convertFacultyIdFromNotOverlapNameメソッド
	 * 全教員で被らない名前から教員IDを取得する.
	 * @param overlapName 全教員で被らない名前
	 * @throws SQLException
	 * @return List<String> 学年歴ID
	 **/
	public String convertFacultyIdFromNotOverlapName(String overlapName) throws SQLException{
		String facultyId = null;
		String sql = "SELECT `FACULTY_ID` FROM `FACULTIES_MST` WHERE `NOT_OVERLAP_NAME` LIKE ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = Connect.getInstance().getConnection().prepareStatement(sql);
			ps.setString(1, overlapName);
			rs = ps.executeQuery();
			if(rs.next()){
				facultyId = rs.getString("FACULTY_ID");
			}
		} finally {
			if(ps != null){
				ps.close();
			}
			if(rs != null){
				rs.close();
			}
		}
		return facultyId;
	}

	/**
	 * createdate : 2015年2月14日
	 * convertFacultyIdFromFacultyIdNumberメソッド
	 * 教員ID番号から教員IDを取得する.
	 * @param deptId 学科Id
	 * @param facultyIdNumber 教員ID番号(複数の場合は「&」で区切られている)
	 * @throws SQLException
	 * @return List<String> 学年歴ID
	 **/
	private String convertFacultyIdFromFacultyIdNumber(String deptId,String facultyIdNumber) throws SQLException{
		//教員が複数いた場合
		String[] facultyIdNumbers = facultyIdNumber.split(MyConfig.SEPARATOR_FACULTY);
		String facultyId = "";
		for(int i=0;i<facultyIdNumbers.length;i++){
			facultyId = facultyId + getFacultyIdFromFacultyIdNumber(deptId,facultyIdNumbers[i]);
			if(facultyIdNumbers.length - 1 != i){
				//区切り文字を追加
				facultyId = facultyId + MyConfig.SEPARATOR_FACULTY;
			}
		}
		return facultyId;
	}
	/**
	 * createdate : 2015年2月14日
	 * getFacultyIdFromFacultyIdNUmberメソッド
	 * 教員ID番号からDBに登録されている教員IDを取得する.
	 * @param deptId 学科Id
	 * @param facultyIdNumber 教員ID番号
	 * @throws SQLException
	 * @return String 教員ID
	 **/
	private String getFacultyIdFromFacultyIdNumber(String deptId,String facultyIdNumber) throws SQLException{
		String facultyId = "";
		String sql = "SELECT `FACULTY_ID` FROM `FACULTIES_MST` WHERE `DEPT_ID` LIKE ? AND `FACULTY_ID_NUMBER` LIKE ?";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = Connect.getInstance().getConnection().prepareStatement(sql);
			ps.setString(1, deptId);
			ps.setString(2, facultyIdNumber);

			rs = ps.executeQuery();
			if(rs.next()){
				facultyId = rs.getString("FACULTY_ID");
			}
		} finally {
			if(ps != null){
				ps.close();
			}
			if(rs != null){
				rs.close();
			}
		}
		return facultyId;
	}

	/**
	 * createdate : 2015年2月14日
	 * getAcademicCalendarIdsメソッド
	 * 学年歴IDを取得する
	 * @param deptId 学科ID
	 * @param year 提供年度
	 * @param semester 学期
	 * @param dayName 曜日名
	 * @throws SQLException
	 * @return List<String> 学年歴ID
	 **/
	private List<String> getAcademicCalendarIds(String deptId,String year,String semester,String dayName) throws SQLException{
		String masterTableName = MyConfig.DB_TABLE_ACADEMIC_CALENDARS_MST;
		List<String> academicCalendarId = new ArrayList<String>();
		String sql = "SELECT `ACADEMIC_CALENDAR_ID` FROM `"+masterTableName+"` "
				+ "WHERE `DEPT_ID` LIKE ? "
				+ "AND `SCHOOL_DAY` = 1 "
				+ "AND `YEAR` LIKE ? "
				+ "AND `SEMESTER` LIKE ? "
				+ "AND `DAY_NAME` LIKE ? "
				+ "ORDER BY `"+masterTableName+"`.`DATE` ASC";

		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = Connect.getInstance().getConnection().prepareStatement(sql);
			ps.setString(1, deptId);
			ps.setString(2, year);
			ps.setString(3, semester);
			ps.setString(4, dayName);
			rs = ps.executeQuery();
			while(rs.next()){
				academicCalendarId.add(rs.getString("ACADEMIC_CALENDAR_ID"));
			}
		} finally {
			if(ps != null){
				ps.close();
			}
			if(rs != null){
				rs.close();
			}
		}
		return academicCalendarId;
	}

	/**
	 * createdate : 2015年2月14日
	 * registTimetableメソッド
	 * 時限情報を登録する.
	 * @param deptId 学科ID
	 * @param year 提供年度
	 * @param timeSection 時間区分
	 * @param timetableName 時限名
	 * @param classStartTime 授業開始時刻
	 * @param classEndTime 授業終了時刻
	 * @throws SQLException
	 **/
	private String registTimetableMst(String deptId,String year,String timeSection,String timetableName,String classStartTime,String classEndTime) throws SQLException{
		String masterTableName = MyConfig.DB_TABLE_TIMETALBES_MST;
		String timetableId = null;

		//登録数
		int registCount = getNowYearMstRegisterCount(masterTableName);
		String digitYear = MyTime.getInstance().getYearTwoDigit();

		timetableId = IdGenerator.generateTimetableId(deptId, digitYear, registCount);

		//インサート結果
		int result = regTimeTable(timetableId,deptId, year, timeSection, timetableName, classStartTime, classEndTime);
		if(result == 0){
			MyLog.getInstance().error("時限ID:"+",学科ID:"+",提供年度:"+",時間区分:"+",時限名:"+",授業開始時間:"+",授業終了時刻");
			timetableId = null;
		}


		return timetableId;
	}

	/**
	 * createdate : 2015年2月14日
	 * regTimeTableメソッド
	 * 時限情報を登録.
	 * @param timetableId 時限ID
	 * @param deptId 学科ID
	 * @param year 提供年度
	 * @param timeSection 時間区分
	 * @param timetableName 時限名
	 * @param classStartTime 授業開始時刻
	 * @param classEndTime 授業終了時刻
	 * @throws SQLException
	 **/
	private int regTimeTable(String timetableId,String deptId,String year,String timeSection,String timetableName,String classStartTime,String classEndTime) throws SQLException{
		int result = 0;

		String sql = "INSERT INTO `TIMETABLES_MST` "
				+ "(`TIMETABLE_ID`, `DEPT_ID`, `ACTIVE_STATUS`, "
				+ "`YEAR`, `TIME_SECTION`, `TIMETABLE_NAME`, "
				+ "`CLASS_START_TIME`, `CLASS_END_TIME`, "
				+ "`RECORED_INSERT_DATE_TIME`, `LAST_UPDATE_TIME`) "
				+ "VALUES (?, ?, '1', ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = Connect.getInstance().getConnection().prepareStatement(sql);
			ps.setString(1,timetableId);
			ps.setString(2,deptId);
			ps.setString(3,year);
			ps.setString(4,timeSection);
			ps.setString(5,timetableName);
			ps.setString(6,classStartTime);
			ps.setString(7,classEndTime);
			result = ps.executeUpdate();
		} finally {
			if(ps != null){
				ps.close();
			}
			if(rs != null){
				rs.close();
			}
		}


		return result;
	}

	/**
	 * createdate : 2015年2月14日
	 * sarchSameRegistTimetableメソッド
	 * 同一内容の時限が存在するかをチェックする
	 * @param deptId 学科ID
	 * @param year 提供年度
	 * @param timeSection 時間区分
	 * @param timetableName 時限名
	 * @param classStartTime 授業開始時刻
	 * @param classEndTime 授業終了時刻
	 * @return String 時限ID
	 * @throws SQLException
	 *	 **/
	private String sarchSameRegistTimetable(String deptId,String year,String timeSection,String timetableName,String classStartTime,String classEndTime) throws SQLException{
		String masterTableName = MyConfig.DB_TABLE_TIMETALBES_MST;
		String timetableId = null;

		String sql = "SELECT TIMETABLE_ID FROM `"+masterTableName+"` "
				+ "WHERE `DEPT_ID` LIKE ? "
				+ "AND `ACTIVE_STATUS` = 1 "
				+ "AND `YEAR` LIKE ? "
				+ "AND `TIME_SECTION` LIKE ? "
				+ "AND `TIMETABLE_NAME` LIKE ? "
				+ "AND `CLASS_START_TIME` = ? "
				+ "AND `CLASS_END_TIME` = ?";

		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = Connect.getInstance().getConnection().prepareStatement(sql);
			ps.setString(1, deptId);
			ps.setString(2, year);
			ps.setString(3, timeSection);
			ps.setString(4, timetableName);
			ps.setString(5, classStartTime);
			ps.setString(6, classEndTime);
			rs = ps.executeQuery();
			if(rs.next()){
				timetableId = rs.getString("TIMETABLE_ID");
			}
		} finally {
			if(ps != null){
				ps.close();
			}
			if(rs != null){
				rs.close();
			}
		}
		return timetableId;
	}

	/**
	 * createdate : 2015年2月14日
	 * getNowYearMstRegisterCountメソッド
	 * 今の年をキーに何件のデータがマスタテーブルに登録されているか調べる.
	 **/
	private int getNowYearMstRegisterCount(String masterTableName) throws SQLException{
		int yearRegistrationCount = 0;
		String sql = "SELECT COUNT(*) FROM `"+masterTableName+"` WHERE `RECORED_INSERT_DATE_TIME` LIKE '%"+MyTime.getInstance().getTwoDigitYear()+"%'";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = Connect.getInstance().getConnection().prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs.next()){
				yearRegistrationCount = rs.getInt("COUNT(*)");
			}
		} finally {
			if(ps != null){
				ps.close();
			}
			if(rs != null){
				rs.close();
			}
		}
		return yearRegistrationCount;
	}

	/**
	 * createdate : 2015年2月14日
	 * serchSameRegistSubjectメソッド
	 * 同一科目情報が存在するかをチェックする
	 * @param deptId 学科ID
	 * @parm year 年度(西暦)
	 * @param subjectNumber 科目番号
	 * @param subjectName 科目名
	 * @param remarks 科目備考
	 * @return subjectId 科目Id
	 * @throws SQLException
	 **/
	private String serchSameRegistSubject(String deptId,String year,String subjectNumber,String subjectName,String remarks) throws SQLException{
		String masterTableName = MyConfig.DB_TABLE_SUBJECTS_MST;
		String subjectId = null;

		String sql = "SELECT SUBJECT_ID FROM `"+masterTableName+"` "
				+ "WHERE `DEPT_ID` LIKE ? "
				+ "AND `ACTIVE_STATUS` = 1 "
				+ "AND `YEAR` LIKE ? "
				+ "AND `SUBJECT_NUMBER` LIKE ? "
				+ "AND `SUBJECT_NAME` LIKE ? "
				+ "AND `REMARKS` LIKE ? ";

		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = Connect.getInstance().getConnection().prepareStatement(sql);
			ps.setString(1, deptId);
			ps.setString(2, year);
			ps.setString(3, subjectNumber);
			ps.setString(4, subjectName);
			ps.setString(5, remarks);
			rs = ps.executeQuery();
			if(rs.next()){
				subjectId = rs.getString("SUBJECT_ID");
			}
		} finally {
			if(ps != null){
				ps.close();
			}
			if(rs != null){
				rs.close();
			}
		}

		return subjectId;
	}

	/**
	 * createdate : 2015年2月14日
	 * registSubjectMstメソッド
	 * 科目情報を登録する.
	 * @param deptId 学科ID
	 * @parm year 年度(西暦)
	 * @param subjectNumber 科目番号
	 * @param subjectName 科目名
	 * @param remarks 科目備考
	 * @return subjectId 科目Id
	 * @throws SQLException
	 **/
	private String registSubjectMst(String deptId,String year,String subjectNumber,String subjectName,String remarks) throws SQLException{
		String masterTableName = MyConfig.DB_TABLE_SUBJECTS_MST;
		String subjectId = null;

		//登録数
		int registCount = getNowYearMstRegisterCount(masterTableName);
		String digitYear = MyTime.getInstance().getYearTwoDigit();

		subjectId = IdGenerator.generateSubjectId(deptId, digitYear, registCount);

		int result = regSubject(subjectId, deptId, year, subjectNumber, subjectName, remarks);
		if(result == 0){
			//登録失敗
			MyLog.getInstance().error("科目ID:"+subjectId+",学科ID:"+deptId+",提供年度:"+year+",科目番号:"+subjectNumber+",科目名:"+subjectName+",授業備考:"+remarks);
			subjectId = null;
		}
		return subjectId;
	}

	/**
	 * createdate : 2015年2月14日
	 * regSubjectメソッド
	 * 科目情報を登録.
	 * @param subjectId 科目ID
	 * @param deptId 学科ID
	 * @parm year 年度(西暦)
	 * @param subjectNumber 科目番号
	 * @param subjectName 科目名
	 * @param remarks 科目備考
	 * @return subjectId 科目Id
	 * @throws SQLException
	 **/
	private int regSubject(String subjectId,String deptId,String year,String subjectNumber,String subjectName,String remarks) throws SQLException{
		int result = 0;
		String sql = "INSERT INTO `SUBJECTS_MST` "
				+ "(`SUBJECT_ID`, `DEPT_ID`, `ACTIVE_STATUS`,"
				+ " `YEAR`, `SUBJECT_NUMBER`, `SUBJECT_NAME`, "
				+ "`REMARKS`, `RECORED_INSERT_DATE_TIME`, `LAST_UPDATE_TIME`) "
				+ "VALUES (?, ?, '1', ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP) ";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = Connect.getInstance().getConnection().prepareStatement(sql);
			ps.setString(1,subjectId);
			ps.setString(2,deptId);
			ps.setString(3,year);
			ps.setString(4,subjectNumber);
			ps.setString(5,subjectName);
			ps.setString(6,remarks);
			result = ps.executeUpdate();
		} finally {
			if(ps != null){
				ps.close();
			}
			if(rs != null){
				rs.close();
			}
		}
		return result;
	}

	/**
	 * createdate : 2015年2月14日
	 * getDeptIdメソッド
	 * DBから学科IDを取得する.
	 **/
	public String getDeptId(String universityName,String deptName) throws SQLException{
		//大学番号
		String univNumber = getUniversityNumber(universityName);
		if(univNumber == null){
			MyLog.getInstance().error("文科省大学番号を取得できません.大学名:「"+universityName+"」");
			return null;
		}

		String deptSystemId = null;
		String sql = "SELECT D.DEPT_ID FROM `DEPT_MST` D,`DEPT_SYSTEM_MST` DS "
				+ "WHERE DS.UNIVERSITY_NUMBER LIKE '"+univNumber+"' "
				+ "AND D.DEPT_SYSTEM_ID = DS.DEPT_SYSTEM_ID "
				+ "AND D.DEPT_NAME LIKE '"+deptName+"'";
		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = Connect.getInstance().getConnection().prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs.next()){
				deptSystemId = rs.getString("DEPT_ID");
			}
		} finally {
			if(ps != null){
				ps.close();
			}
			if(rs != null){
				rs.close();
			}
		}
		return deptSystemId;
	}

	/**
	 * createdate : 2015年2月14日
	 * getUniversityNumberメソッド
	 * DBから大学IDを取得する.
	 * @param universityName 大学名
	 **/
	private String getUniversityNumber(String universityName) throws SQLException{
		String universityNumber = null;
		String sql = "SELECT `UNIVERSITY_NUMBER` FROM `UNIVERSITY_MINISTRY_MST` WHERE `UNIVERSITY_NAME` LIKE '"+universityName+"'";

		PreparedStatement ps = null;
		ResultSet rs = null;
		try{
			ps = Connect.getInstance().getConnection().prepareStatement(sql);
			rs = ps.executeQuery();
			if(rs.next()){
				universityNumber = rs.getString("UNIVERSITY_NUMBER");
			}
		} finally {
			if(ps != null){
				ps.close();
			}
			if(rs != null){
				rs.close();
			}
		}
		return universityNumber;
	}
}

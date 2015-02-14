package jp.pmw.class_register;

import java.util.List;
import jp.pmw.mysql.Connect;
import com.jenkov.db.PersistenceManager;
import com.jenkov.db.itf.IDaos;
import com.jenkov.db.itf.PersistenceException;

public class TmpData<T>{
	/**
	 * createdate : 2015年2月14日
	 * getTmpMstメソッド
	 * DBのTMP_CLSSS_SCHEDULE_MSTからデータを取得するためのORM
	 **/
	public List<T> getTmpMst(Class<T> clazz,String tmpMstTableName) throws PersistenceException{
		PersistenceManager manager = new PersistenceManager();
		IDaos daos = manager.createDaos(Connect.getInstance().getConnection());
		List<T> tempMst = daos.getObjectDao().readList(clazz, "SELECT * FROM `"+tmpMstTableName+"` WHERE `COMPLETE_FLAG` = ? ORDER BY `RECORED_INSERT_DATE_TIME` ASC", 0);
		return tempMst;
	}
}

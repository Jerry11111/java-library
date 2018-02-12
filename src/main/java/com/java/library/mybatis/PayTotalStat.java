package com.java.library.mybatis;

import java.sql.Timestamp;

public class PayTotalStat {
	public Timestamp cdate;
	public int hour;
	public int operatorId;
	public int areaId;
	public long gameId;
	public String channelName;
	public int payUserCnt;
	
	public static class PayTotalStatKey {
		public Timestamp cdate;
		public int hour;
		public int operatorId;
		public int areaId;
		public long gameId;
		public String channelName;
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + areaId;
			result = prime * result + ((cdate == null) ? 0 : cdate.hashCode());
			result = prime * result + ((channelName == null) ? 0 : channelName.hashCode());
			result = prime * result + (int) (gameId ^ (gameId >>> 32));
			result = prime * result + hour;
			result = prime * result + operatorId;
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			PayTotalStatKey other = (PayTotalStatKey) obj;
			if (areaId != other.areaId)
				return false;
			if (cdate == null) {
				if (other.cdate != null)
					return false;
			} else if (!cdate.equals(other.cdate))
				return false;
			if (channelName == null) {
				if (other.channelName != null)
					return false;
			} else if (!channelName.equals(other.channelName))
				return false;
			if (gameId != other.gameId)
				return false;
			if (hour != other.hour)
				return false;
			if (operatorId != other.operatorId)
				return false;
			return true;
		}
		
		
	}
	

}

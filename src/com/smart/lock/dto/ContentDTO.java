package com.smart.lock.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.smart.lock.common.SlideConstants;
import com.smart.lock.utils.DisplayUtils;

public class ContentDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int id;

	private int type;

	private String image;

	private String localPath;

	private int localViewCount;

	private boolean isFavorite;

	private boolean isDislike;

	private String link;

	private String title;

	private BigDecimal bonusAmount;

	private int accountRange;

	private String content;

	private Date startTime;

	private Date endTime;

	private int status;

	private int accountAssnType;

	public int getAccountAssnType() {
		return accountAssnType;
	}

	public void setAccountAssnType(int accountAssnType) {
		this.accountAssnType = accountAssnType;
	}

	private Date addTime;

	private Date updateTime;

	private Date downloadTime;

	private List<String> tags;

	private List<Integer> categoryIds;

	private int priority;

	/** default 1*/
	private int hasMore;



	public List<Integer> getCategoryIds() {
		return categoryIds;
	}

	public String getCategoryStr() {
		String ret = "";
		if (categoryIds != null && categoryIds.size() > 0) {
			for (int tag : categoryIds) {
				ret += (ret.length() == 0 ? "" : ",") + tag;
			}
		}
		return ret;
	}

	public void setCategoryIds(List<Integer> categoryIds) {
		this.categoryIds = categoryIds;
	}

	public void setHasMore(int hasMore) {
		this.hasMore = hasMore;
	}

	public int getHasMore() {
		return hasMore;
	}

	public Date getDownloadTime() {
		return downloadTime;
	}

	public void setDownloadTime(Date downloadTime) {
		this.downloadTime = downloadTime;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public ContentDTO() {
		this.addTime = Calendar.getInstance().getTime();
		this.downloadTime = this.addTime;
		this.tags = new ArrayList<String>();
		this.categoryIds = new ArrayList<Integer>();
	}

	public ContentDTO(int id, int type, String image, String link, String title) {
		this.id = id;
		this.type = type;
		this.image = image;
		this.localPath = "preLoad";
		this.localViewCount = 0;
		this.isFavorite = true;
		this.isDislike = false;
		this.link = link;
		this.title = title;
		this.bonusAmount = new BigDecimal(0);
		this.content = "";

		Calendar c = Calendar.getInstance();

		this.startTime = c.getTime();
		c.add(Calendar.YEAR, 1);
		this.endTime = c.getTime();
		this.addTime = Calendar.getInstance().getTime();
		this.updateTime = this.addTime;
		this.downloadTime = this.addTime;
		this.tags = new ArrayList<String>();
		this.categoryIds = new ArrayList<Integer>();
	}

	public ContentDTO(int id) {
		this.id = id;
	}

	public ContentDTO(int id, int type, String image, String localPath,
					  int localViewCount, int isFavorite, int isDislike, String link,
					  String title, BigDecimal bonusAmount, String content,
					  String startTime, String endTime, int status, String addTime,
					  String updateTime, String tags, int priority, String downloadTime, int hasMore, String categoryIds) {
		this.id = id;
		this.type = type;
		this.image = image;
		this.localPath = localPath;
		this.localViewCount = localViewCount;
		this.isFavorite = isFavorite == 1;
		this.isDislike = isDislike == 1;
		this.link = link;
		this.title = title;
		this.bonusAmount = bonusAmount;
		this.content = content;
		this.priority = priority;
		this.hasMore = hasMore;
		this.startTime = DisplayUtils.parseDatetime(startTime);
		this.endTime = DisplayUtils.parseDatetime(endTime);
		this.addTime = DisplayUtils.parseDatetime(addTime);
		this.updateTime = DisplayUtils.parseDatetime(updateTime);
		this.downloadTime = DisplayUtils.parseDatetime(downloadTime);
		this.tags = new ArrayList<String>();
		this.categoryIds = new ArrayList<Integer>();
		this.status = status;
		if (tags != null && tags.length() > 0) {
			for (String tag : tags.split(",")) {
				this.tags.add(tag);
			}
		}
		if (categoryIds != null && categoryIds.length() > 0) {
			for (String categorys : categoryIds.split(",")) {
				this.categoryIds.add(Integer.parseInt(categorys));
			}
		}
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	public int getLocalViewCount() {
		return localViewCount;
	}

	public void setLocalViewCount(int localViewCount) {
		this.localViewCount = localViewCount;
	}

	public boolean isFavorite() {
		return isFavorite;
	}

	public void setFavorite(boolean isFavorite) {
		this.isFavorite = isFavorite;
	}

	public boolean isDislike() {
		return isDislike;
	}

	public void setDislike(boolean isDislike) {
		this.isDislike = isDislike;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public BigDecimal getBonusAmount() {
		return bonusAmount;
	}

	public void setBonusAmount(BigDecimal bonusAmount) {
		this.bonusAmount = bonusAmount;
	}

	public int getAccountRange() {
		return accountRange;
	}

	public void setAccountRange(int accountRange) {
		this.accountRange = accountRange;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public List<String> getTags() {
		return tags;
	}

	public String getTagStr() {
		String ret = "";
		if (tags != null && tags.size() > 0) {
			for (String tag : tags) {
				ret += (ret.length() == 0 ? "" : ",") + tag;
			}
		}
		return ret;
	}

	public void setTags(List<String> tags) {
		this.tags = tags;
	}

	public Date getAddTime() {
		return addTime;
	}

	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

}

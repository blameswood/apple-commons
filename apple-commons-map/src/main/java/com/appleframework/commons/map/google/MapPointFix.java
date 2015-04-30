package com.appleframework.commons.map.google;

import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.appleframework.commons.map.utility.DownloadUtility;

/**
 * ��ȡ������ĵ�
 * @
 *
 */
public class MapPointFix {
	
	private static Logger logger = Logger.getLogger(MapPointFix.class);

    /**
     * ���ƫ���
     */
    private static Map<String, BigDecimal[]> pDeviation = new HashMap<String, BigDecimal[]>();
    
	private String tableFilePath;

	public void setTableFilePath(String tableFilePath) {
		this.tableFilePath = tableFilePath;
	}
        
    /**
     * ��ȡƫ�������ƫ��
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public void init() throws Exception {
    	logger.info("read point deviation begin......");
        long beginTime = System.currentTimeMillis();
        /*List<String> points = FileUtils.readLines(new File("C:/table.txt"));
        List<String> points = FileUtils.readLines(
                FileUtils.toFile(this.getClass().getClassLoader().getResource("table.txt")));*/
        List<String> points = null;
		if(null == tableFilePath || tableFilePath.isEmpty()) {
			points = FileUtils.readLines(FileUtils.toFile(this.getClass().getClassLoader().getResource("table.txt")));
		}
		else {
			File tableFile = new File(tableFilePath);
			if(!tableFile.exists()){
				int index = tableFilePath.indexOf("table.txt");
				String filePathString = tableFilePath.substring(0, index);
				File tableFilePathFile = new File(filePathString);
				if(!tableFilePathFile.exists()){
					tableFilePathFile.mkdir();
				}
				String url = "http://www.appleframework.com/map/table.txt";
				try {
					DownloadUtility.downloadFile(url, filePathString);
				} catch (Exception e) {
					logger.error(e);
				}
			}
			points = FileUtils.readLines(new File(tableFilePath));
		}
		
        int x = 719;
        int y = 99;
        BigDecimal pointXStandard = null;
        BigDecimal pointYStandard = null;
        //ÿ�������׼���ƫ��
        BigDecimal[] pd = null;
        String[] pointXY;
        for (String pointStr : points) {
            //ÿ��pointXY��������
        	pointXY = StringUtils.trim(pointStr).split("\\s+");
            x += 1;
            if ("7200000".equals(pointXY[0])) {
                x = 720;
                y += 1;
                pointYStandard = new BigDecimal(y).divide(new BigDecimal(10));
            }
            
            //�����һ�����ƫ
            pointXStandard = new BigDecimal(x).divide(new BigDecimal(10));
            pd = new BigDecimal[] {new BigDecimal(pointXY[0]).divide(new BigDecimal(100000)).subtract(pointXStandard),
                    new BigDecimal(pointXY[1]).divide(new BigDecimal(100000)).subtract(pointYStandard)};
            pDeviation.put(String.valueOf(x) + y, pd);
            //����ڶ������ƫ
            x += 1;
            pointXStandard = new BigDecimal(x).divide(new BigDecimal(10));
            pd = new BigDecimal[] {new BigDecimal(pointXY[2]).divide(new BigDecimal(100000)).subtract(pointXStandard),
                    new BigDecimal(pointXY[3]).divide(new BigDecimal(100000)).subtract(pointYStandard)};
            pDeviation.put(String.valueOf(x) + y, pd);
        }
        logger.info("read point deviation end...... " + (System.currentTimeMillis() - beginTime) + "ms");
    }
    
    /**
     * ��ȡ�������
     * @param longitude
     * @param latitude
     * @param toGMap false��ͼ��ʵ trueʵ�ʵ���
     * @return
     */
    public static String[] getFixedPoint(String longitude, String latitude, boolean toGMap) {
        longitude = StringUtils.trim(longitude).replaceAll("E", "");
        latitude = StringUtils.trim(latitude).replaceAll("N", "");
        int i = longitude.indexOf(".");
        int j = latitude.indexOf(".");
        String[] point = new String[] {longitude, latitude};
        
        //���ƫ����е�key
        if(i!=-1 && j!=-1){
			String pDeviationKey = longitude.substring(0, i)
					+ longitude.substring(i + 1, i + 2)
					+ latitude.substring(0, j)
					+ latitude.substring(j + 1, j + 2);
	        BigDecimal[] pd = pDeviation.get(pDeviationKey);
	        
	        if (pd != null) {
	            if (toGMap) {
	                point = new String[] {new BigDecimal(longitude).add(pd[0]).toString(),
	                        new BigDecimal(latitude).add(pd[1]).toString()};
	            } else {
	                point = new String[] {new BigDecimal(longitude).subtract(pd[0]).toString(),
	                        new BigDecimal(latitude).subtract(pd[1]).toString()};
	            }
	        }
        }
        return point;
    }
    
    public static void main(String[] args) {
    	MapPointFix fix = new MapPointFix();
    	fix.setTableFilePath("/temp/table.txt");
    	try {
			fix.init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
    
}

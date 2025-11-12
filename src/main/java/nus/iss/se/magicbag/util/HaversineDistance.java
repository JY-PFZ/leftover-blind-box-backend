package nus.iss.se.magicbag.util;

public class HaversineDistance {

    private HaversineDistance() {
    }

    private static final double EARTH_RADIUS_KM = 6371.0; // 地球半径，单位：公里

    /**
     * 计算两个经纬度坐标之间的距离（单位：公里）
     *
     * @param lat1 第一个点的纬度（十进制度数）
     * @param lon1 第一个点的经度（十进制度数）
     * @param lat2 第二个点的纬度（十进制度数）
     * @param lon2 第二个点的经度（十进制度数）
     * @return 距离，单位为公里
     */
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 将角度转换为弧度
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // 纬度差和经度差
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLon = lon2Rad - lon1Rad;

        // Haversine 公式
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                        Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    // 示例：测试北京到上海的距离
/*    public static void main(String[] args) {
        // 北京 (39.9042° N, 116.4074° E)
        double beijingLat = 39.9042;
        double beijingLon = 116.4074;

        // 上海 (31.2304° N, 121.4737° E)
        double shanghaiLat = 31.2304;
        double shanghaiLon = 121.4737;

        double distance = calculateDistance(beijingLat, beijingLon, shanghaiLat, shanghaiLon);

        System.out.printf("北京到上海的距离约为: %.2f 公里%n", distance);
    }*/
}

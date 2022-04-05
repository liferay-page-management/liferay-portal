import com.liferay.portal.util.PropsUtil;
import java.util.Properties;

Properties properties = new Properties();
properties.setProperty("feature.flag.LPS-141410","true");
PropsUtil.addProperties(properties);
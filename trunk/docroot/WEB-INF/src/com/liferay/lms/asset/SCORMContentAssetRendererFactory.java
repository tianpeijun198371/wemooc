package com.liferay.lms.asset;

import javax.portlet.PortletMode;
import javax.portlet.PortletRequest;
import javax.portlet.PortletURL;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import com.liferay.lms.model.SCORMContent;
import com.liferay.lms.service.SCORMContentLocalServiceUtil;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.portlet.LiferayPortletRequest;
import com.liferay.portal.kernel.portlet.LiferayPortletResponse;
import com.liferay.portal.kernel.portlet.LiferayWindowState;
import com.liferay.portal.kernel.portlet.PortletModeFactory;
import com.liferay.portal.kernel.portlet.WindowStateFactory;
import com.liferay.portal.kernel.util.WebKeys;
import com.liferay.portal.security.permission.PermissionChecker;
import com.liferay.portal.theme.ThemeDisplay;
import com.liferay.portlet.PortletURLFactoryUtil;
import com.liferay.portlet.asset.model.AssetRenderer;
import com.liferay.portlet.asset.model.AssetRendererFactory;
import com.liferay.portlet.asset.model.BaseAssetRendererFactory;

public class SCORMContentAssetRendererFactory extends BaseAssetRendererFactory
 {

	public static final String CLASS_NAME = SCORMContent.class.getName();
	public static final String TYPE = "scorm";

	@Override
	public String getClassName() {
	
		return CLASS_NAME;
	}

	public AssetRenderer getAssetRenderer(long classPK, int type)
	throws PortalException, SystemException {
	SCORMContent scorm = SCORMContentLocalServiceUtil.getSCORMContent(classPK);
	
		return new SCORMContentAssetRenderer(scorm);
	}
	public PortletURL getURLAdd(
            LiferayPortletRequest liferayPortletRequest,
            LiferayPortletResponse liferayPortletResponse)
        {
		
        return null;
/*
        HttpServletRequest request =
            liferayPortletRequest.getHttpServletRequest();

        ThemeDisplay themeDisplay = (ThemeDisplay)request.getAttribute(
            WebKeys.THEME_DISPLAY);

      try
      {
    	  PortletURL portletURL= 
    			  PortletURLFactoryUtil.create(request,"lmsactivitieslist_WAR_liferaylmsportlet",getControlPanelPlid(themeDisplay), PortletRequest.RENDER_PHASE);
          portletURL.setParameter("mvcPath", "/html/lmsactivitieslist/editactivity.jsp");
         return portletURL;
      }
      catch(Exception e)
      {
    	  e.printStackTrace();
    	  
      }
        return null;
        */
    }
	@Override
	public boolean hasPermission(PermissionChecker permissionChecker,
			long classPK, String actionId) throws Exception {
	
		SCORMContent scorm=SCORMContentLocalServiceUtil.getSCORMContent(classPK);
		return permissionChecker.hasPermission(scorm.getGroupId(), SCORMContent.class.getName(), classPK,actionId);

	}

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isSelectable() {
		return true;
	}

	



}

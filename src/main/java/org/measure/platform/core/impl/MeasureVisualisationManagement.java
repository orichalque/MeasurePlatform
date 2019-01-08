package org.measure.platform.core.impl;

import java.util.Locale;

import javax.inject.Inject;

import org.measure.platform.core.api.IMeasureVisaulisationManagement;
import org.measure.platform.core.api.entitys.MeasureInstanceService;
import org.measure.platform.core.api.entitys.MeasureViewService;
import org.measure.platform.core.entity.AnalysisCard;
import org.measure.platform.core.entity.MeasureInstance;
import org.measure.platform.core.entity.MeasureView;
import org.measure.platform.core.entity.Project;
import org.measure.platform.service.measurement.api.IElasticsearchIndexManager;
import org.measure.smm.measure.model.DataSource;
import org.measure.smm.measure.model.Layout;
import org.measure.smm.measure.model.SMMMeasure;
import org.measure.smm.measure.model.View;
import org.measure.smm.measure.model.ViewTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
public class MeasureVisualisationManagement implements IMeasureVisaulisationManagement {
	
	
    @Autowired
    private MessageSource messageSource;

    @Value("${measure.kibana.adress}")
    private String kibanaAdress;
    
    @Inject
    private IElasticsearchIndexManager indexManager;
        
    @Inject 
    private MeasureInstanceService measureInstanceService; 
    
 
    @Override
	public String formatViewDataAsKibanaURL(MeasureView measureView) {
        String type = "line";
        
        if (measureView.getType().equals(ViewTypeEnum.DATA)) {
        
            String refresh = measureView.isAuto() ? "f" : "t";
            String periode = "from:now-1y,mode:quick,to:now";
            String measure = measureView.getMeasureinstance().getInstanceName().replaceAll(" ", "+");  
            String visualisedProperty = measureView.getVisualisedProperty();
            String color = measureView.getColor();
            String font = measureView.getFontSize();    
            String width = measureView.getWidth();
            String height = measureView.getHeight();
   
            return messageSource.getMessage("viewtype.view2", new Object[] { "metric", refresh, periode,measure, font, height, width, kibanaAdress, visualisedProperty, color,indexManager.getBaseMeasureIndex() }, Locale.ENGLISH);
        } else {
            if (measureView.getType().equals(ViewTypeEnum.LIGNE.toString())) {
                type = "line";
            } else if (measureView.getType().equals(ViewTypeEnum.AREA.toString())) {
                type = "area";
            } else if (measureView.getType().equals(ViewTypeEnum.BAR.toString())) {
                type = "histogram";
            }
        
            String refresh = measureView.isAuto() ? "f" : "t";        
            String periode = measureView.getTimePeriode();
            String interval = measureView.getTimeAgregation();    
            String measure = measureView.getMeasureinstance().getInstanceName().replaceAll(" ", "+");   
            String color = measureView.getColor();
            String width = measureView.getWidth();
            String height = measureView.getHeight();
            String visualisedProperty = measureView.getVisualisedProperty();
            String dateIndex = measureView.getDateIndex();
        
            return messageSource.getMessage("viewtype.view1", new Object[] { type, refresh, periode, measure,color, interval, height, width, kibanaAdress, visualisedProperty, dateIndex,indexManager.getBaseMeasureIndex() }, Locale.ENGLISH);
        }
    }

    @Override
    public String formatViewDataAsKibanaViewReference(MeasureView measureView) {
        String width = measureView.getWidth();
        String height = measureView.getHeight();        
        String periode = measureView.getTimePeriode();
        String refresh = measureView.isAuto() ? "f" : "t";
            
        return messageSource.getMessage("viewtype.view3",new Object[] { height, width, kibanaAdress, measureView.getKibanaName(),refresh ,periode}, Locale.ENGLISH);
    }

    @Override
    public String formatViewDataAsKibanaDashboardReference(MeasureView measureView) {
        String height = measureView.getHeight();               
        String periode = measureView.getTimePeriode();
        String refresh = measureView.isAuto() ? "f" : "t";       
        return messageSource.getMessage("viewtype.view4",new Object[] { height, kibanaAdress, measureView.getKibanaName(),refresh,periode }, Locale.ENGLISH);
    }
    

    @Override
    public String formatViewDataAsAnalysisCard(MeasureView measureView) {
       AnalysisCard card = measureView.getAnalysiscard();
       return messageSource.getMessage("viewtype.view5", new Object[] {card.getCardUrl(),card.getPreferedHeight(), card.getPreferedWidth()}, Locale.ENGLISH);
	}
 
    @Override
    public MeasureView createDefaultMeasureView(SMMMeasure measure,Long measureInstanceId) {
    	if(measure.getViews() != null) {
    		for(View mView : measure.getViews().getView())	{
    			if(mView.isDefault()) {
    		    	MeasureInstance instance = measureInstanceService.findOne(measureInstanceId);
    		    	Project project = instance.getProject();
    				return createMeasureView(mView,project,instance);
    			}
    		}
    	}
    	return null;
    }
    
    @Override
    public MeasureView createDefaultMeasureView(SMMMeasure measure,Long measureInstanceId,String viewName) {
    	if(measure.getViews() != null) {
    		for(View mView : measure.getViews().getView())	{
    			if(mView.getName().equals(viewName)) {
    		    	MeasureInstance instance = measureInstanceService.findOne(measureInstanceId);
    		    	Project project = instance.getProject();
    		    	return createMeasureView(mView,project,instance);
    			}
    		}
    	}
    	return null;
    }
    
    private MeasureView createMeasureView(View mView,Project project,MeasureInstance measure) {
    	MeasureView measureView = new MeasureView();
     	measureView.setMode("AUTO");
    	measureView.setAuto(mView.isAutoRefresh());
    	measureView.setType(mView.getType().toString());
    	measureView.setName(mView.getName());
    	measureView.setDescription(mView.getDescription());
    	
    	if(mView.getDatasource() != null) {
    		DataSource dsView = mView.getDatasource() ;
    		measureView.setViewData(dsView.getDataIndex());
    		measureView.setDateIndex(dsView.getDateIndex());
    		measureView.setTimePeriode("from:now-"+dsView.getTimePeriode()+",mode:quick,to:now");
    		measureView.setTimeAgregation(dsView.getTimeAggreation());
    	}
    	if(mView.getLayout() != null) {
    		Layout layout = mView.getLayout();
    		measureView.setWidth(layout.getWidth());
    		measureView.setHeight(layout.getHeight());
    		measureView.setFontSize(layout.getFontSize());
    	}  	
    	measureView.setProject(project);
    	measureView.setMeasureinstance(measure); 
    	
    	return measureView;
    }
}

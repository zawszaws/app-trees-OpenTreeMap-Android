package org.azavea.otm;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.azavea.otm.data.Model;
import org.azavea.otm.data.Plot;
import org.azavea.otm.data.User;
import org.json.JSONException;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FieldGroup {
	
	private String title;
	private Map<String,Field> fields = new LinkedHashMap<String, Field>();
	private enum DisplayMode { VIEW, EDIT};
	
	public FieldGroup() {
		this.title = "";
	}
	
	public FieldGroup(String title) {
		this.title = title;
	}
	
	public void addFields(Map<String,Field> fields) {
		this.fields = fields;
	}

	public void addFields(NodeList fieldDefs) {
		if (fieldDefs != null) {
			for (int i=0; i < fieldDefs.getLength(); i++) {
				Node def = fieldDefs.item(i);
				if (def.getNodeType() == Node.ELEMENT_NODE) {
					addField(Field.makeField(def));
				}
			}
		}
	}
	
	public void addField(Field field) {
		this.fields.put(field.key, field);
	}

	public String getTitle() {
		return title;
	}

	public Map<String,Field> getFields() {
		return fields;
	}

	private View render(LayoutInflater layout, Plot model, DisplayMode mode, User user, Context context) {
		

		View container = layout.inflate(R.layout.plot_field_group, null);
		LinearLayout group = (LinearLayout)container.findViewById(R.id.field_group); 
		View fieldView = null;
		int renderedFieldCount = 0;

		
        ((TextView)group.findViewById(R.id.group_name)).setText(this.title);
        if (this.title != null) {
	        for (Entry<String, Field> field : fields.entrySet()) {
	        	try {
	        		fieldView = null;
	        		switch (mode) {
	        			case VIEW:
	        				if (!field.getValue().editViewOnly) {
	        					fieldView = field.getValue().renderForDisplay(layout, model, context);
	        				}
	        				break;
	        			case EDIT:
	        				fieldView= field.getValue().renderForEdit(layout, model, user, context);
	        				break;
	        		}
	        		
	        		if (fieldView != null) {
	        			renderedFieldCount++;
	        			group.addView(fieldView);
	        		}
	        		
	        	} catch (JSONException e) {
	        		Log.d(App.LOG_TAG, "Error rendering field '" + field.getKey() + "' " + e.getMessage());
	        	}
			}
        }
        if (renderedFieldCount > 0 ) {
        	return group;
        } else { 
        	return null;
        }
	}
	
	/**
	 * Render a field group and its child fields for viewing
	 * @throws JSONException 
	 */
	public View renderForDisplay(LayoutInflater layout, Plot model, Context context) {
		return render(layout, model, DisplayMode.VIEW, null, context);
	}
	
	/**
	 * Render a field group and its child fields for editing
	 * @throws JSONException 
	 */
	public View renderForEdit(LayoutInflater layout, Plot model, User user, Context context) {
        return render(layout, model, DisplayMode.EDIT, user, context);
	}

	public void update(Model model) throws Exception {
		for (Entry<String, Field> field : fields.entrySet()) {
			field.getValue().update(model);
		}
	}	
}

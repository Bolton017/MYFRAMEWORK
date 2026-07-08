package view;

import java.util.Map;

public class ModelAndView {
    private String view;
    private Map<String, Object> data;

    public void addObject(String non, Object obj) {
        data.put(non, obj);
    }

    public ModelAndView(String view) {
        this.view = view;
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public Map<String, Object> getData() {
        return data;
    }
}
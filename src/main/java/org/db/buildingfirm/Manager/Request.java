package org.db.buildingfirm.Manager;

import javafx.beans.property.*;

public class Request {
    private final IntegerProperty id      = new SimpleIntegerProperty();
    private final StringProperty  login   = new SimpleStringProperty();
    private final StringProperty  type    = new SimpleStringProperty();
    private final StringProperty  status  = new SimpleStringProperty();
    private final StringProperty  created = new SimpleStringProperty();

    public Request(int id, String login, String type, String status, String created) {
        this.id.set(id);
        this.login.set(login);
        this.type.set(type);
        this.status.set(status);
        this.created.set(created);
    }

    public IntegerProperty idProperty()      { return id; }
    public StringProperty  loginProperty()   { return login; }
    public StringProperty  typeProperty()    { return type; }
    public StringProperty  statusProperty()  { return status; }
    public StringProperty  createdProperty() { return created; }

    public int    getId()      { return id.get(); }
    public String getLogin()   { return login.get(); }
    public String getType()    { return type.get(); }
    public String getStatus()  { return status.get(); }
    public String getCreated() { return created.get(); }
}

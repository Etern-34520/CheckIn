package indi.etern.checkIn.entities.linkUtils;

public interface LinkSource<L extends Link<?,?>> {
    L getLinkWrapper();
    void setLinkWrapper(L linkWrapper);
}

package ru.mfa.dto;

public class TicketResponse {

    private Ticket ticket;
    private String digitalSignature;

    public TicketResponse() {}

    public Ticket getTicket() { return ticket; }
    public void setTicket(Ticket ticket) { this.ticket = ticket; }

    public String getDigitalSignature() { return digitalSignature; }
    public void setDigitalSignature(String digitalSignature) { this.digitalSignature = digitalSignature; }
}

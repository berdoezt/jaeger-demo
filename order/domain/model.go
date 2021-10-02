package domain

type Order struct {
	Id        int    `json:"id"`
	Price     int    `json:"price"`
	OrderType string `json:"type"`
	Name      string `json:"name"`
}

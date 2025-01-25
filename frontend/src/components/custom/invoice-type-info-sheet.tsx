import {
    Accordion,
    AccordionContent,
    AccordionItem,
    AccordionTrigger,
} from "@/components/ui/accordion"
import {
    Sheet,
    SheetContent,
    SheetDescription,
    SheetHeader,
    SheetTitle,
    SheetTrigger,
} from "@/components/ui/sheet"

import { invoiceTypeMappings, useInvoiceTypeTranslator } from "@/utils/invoice-type-utils"
import { QuestionMarkCircleIcon } from "@heroicons/react/24/outline"
import { useTranslation } from "react-i18next"



export const InvoiceTypeInfoSheet: React.FC = () => {
    const { t, } = useTranslation()
    const { translateInvoiceType, getInvoiceTypeDescription } = useInvoiceTypeTranslator()
    return (
        <Sheet>
            <SheetTrigger className="p-0 w-auto h-auto inline-flex items-center justify-center align-middle pb-[2px] focus-visible:border-none hover:border-none border-none focus:border-none blur:border-none" >
                <QuestionMarkCircleIcon className="w-5 h-5 hover:text-yellow-400 transition-colors" />
            </SheetTrigger>


            <SheetContent>
                <SheetHeader>
                    <SheetTitle>{t("info-sheet-header")}</SheetTitle>
                    <SheetDescription>
                        <Accordion type="single" collapsible>
                            {Array.from(invoiceTypeMappings.entries()).map(([typeCode, translationIdentifier]) => {
                                return (
                                    <AccordionItem value={String(typeCode)} key={typeCode}>
                                        <AccordionTrigger className="hover:border-yellow-400">{translateInvoiceType(typeCode)}</AccordionTrigger>
                                        <AccordionContent>
                                            {getInvoiceTypeDescription(typeCode)}
                                        </AccordionContent>
                                    </AccordionItem>
                                );
                            })}
                        </Accordion>
                    </SheetDescription>
                </SheetHeader>
            </SheetContent>
        </Sheet>
    )
}
